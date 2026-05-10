package com.esl.service;

import com.esl.dao.repository.VocabInterpretationRepository;
import com.esl.entity.VocabInterpretation;
import com.esl.service.rest.ReplicateAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterpretationServiceTest {

    @Mock VocabInterpretationRepository repository;
    @Mock ReplicateAIService replicateAIService;

    InterpretationService service;

    @BeforeEach
    void setUp() {
        service = new InterpretationService(repository, replicateAIService);
    }

    @Test
    void interpret_dbHit_returnsCachedAndDoesNotCallReplicate() {
        var row = new VocabInterpretation("elephant", "zh-Hant", "大象");
        when(repository.findByTextAndLang("elephant", "zh-Hant")).thenReturn(Optional.of(row));

        var result = service.interpret("elephant", "zh-Hant");

        assertEquals("大象", result);
        verify(replicateAIService, never()).getInterpretation(anyString(), anyString());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("dbMissCases")
    void interpret_dbMiss_callsReplicateAndPersists(String text, String lang, String expected) {
        when(repository.findByTextAndLang(text, lang)).thenReturn(Optional.empty());
        when(replicateAIService.getInterpretation(text, lang)).thenReturn(expected);

        var result = service.interpret(text, lang);

        assertEquals(expected, result);
        var captor = ArgumentCaptor.forClass(VocabInterpretation.class);
        verify(repository).save(captor.capture());
        var saved = captor.getValue();
        assertEquals(text, saved.getText());
        assertEquals(lang, saved.getLang());
        assertEquals(expected, saved.getResult());
    }

    static Stream<Arguments> dbMissCases() {
        return Stream.of(
                Arguments.of("elephant", "zh-Hant", "大象"),
                Arguments.of("hello", "zh-Hans", "你好"),
                Arguments.of("elephant", "en", "A large mammal with tusks.")
        );
    }

    @Test
    void interpret_normalizesInput_beforeLookupAndReplicate() {
        when(repository.findByTextAndLang(anyString(), anyString())).thenReturn(Optional.empty());
        when(replicateAIService.getInterpretation(anyString(), anyString())).thenReturn("result");

        service.interpret("  Hello   World  ", "en");

        verify(repository).findByTextAndLang("hello world", "en");
        verify(replicateAIService).getInterpretation("hello world", "en");
    }

    @ParameterizedTest
    @MethodSource("emptyOrOversizeInputs")
    void interpret_emptyOrOversizeInput_returnsEmptyAndDoesNotCallReplicate(String input) {
        assertEquals("", service.interpret(input, "en"));
        verifyNoInteractions(replicateAIService);
        verifyNoInteractions(repository);
    }

    static Stream<Arguments> emptyOrOversizeInputs() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(""),
                Arguments.of("   "),
                Arguments.of("a".repeat(InterpretationService.MAX_TEXT_LENGTH + 1))
        );
    }

    @Test
    void interpret_unsupportedLang_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.interpret("hello", "fr"));
        verifyNoInteractions(replicateAIService);
    }

    @Test
    void interpret_duplicateKeyRace_returnsLocalResult() {
        when(repository.findByTextAndLang(anyString(), anyString())).thenReturn(Optional.empty());
        when(replicateAIService.getInterpretation("elephant", "zh-Hant")).thenReturn("local-result");
        doThrow(new DataIntegrityViolationException("duplicate key")).when(repository).save(any());

        var result = service.interpret("elephant", "zh-Hant");

        assertEquals("local-result", result);
        verify(repository, times(1)).findByTextAndLang(anyString(), anyString()); // no refetch
    }

    @ParameterizedTest(name = "normalize(\"{0}\") -> \"{1}\"")
    @MethodSource("normalizationCases")
    void normalize_returnsCanonicalForm(String input, String expected) {
        assertEquals(expected, InterpretationService.normalize(input));
    }

    static Stream<Arguments> normalizationCases() {
        return Stream.of(
                Arguments.of("Hello  world", "hello world"),
                Arguments.of("  HELLO\tworld  ", "hello world"),
                Arguments.of("The Cat SAT on the", "the cat sat on the"),
                // NFD (5 chars: c, a, f, e, COMBINING_ACUTE_ACCENT) -> NFC (4 chars: c, a, f, é)
                Arguments.of("café", "café")
        );
    }
}
