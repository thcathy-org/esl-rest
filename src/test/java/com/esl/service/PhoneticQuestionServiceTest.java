package com.esl.service;

import com.esl.dao.PhoneticQuestionDAO;
import com.esl.entity.rest.DictionaryResult;
import com.esl.model.PhoneticQuestion;
import com.esl.service.rest.WebParserRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PhoneticQuestionServiceTest {

    private WebParserRestService webService;
    private PhoneticQuestionDAO phoneticQuestionDAO;
    private PhoneticQuestionService service;

    @BeforeEach
    void setUp() {
        webService = mock(WebParserRestService.class);
        phoneticQuestionDAO = mock(PhoneticQuestionDAO.class);
        service = new PhoneticQuestionService();
        setField(service, "webService", webService);
        setField(service, "phoneticQuestionDAO", phoneticQuestionDAO);
        setField(service, "restTemplate", mock(RestTemplate.class));
    }

    @Test
    void buildQuestionByWebAPI_whenDictionaryResolvesToDifferentWord_shouldSkipPronunciationUrlAndNotPersist() {
        when(webService.queryDictionary("learning"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(
                        new DictionaryResult("learn", "https://example.com/learn.mp3", "en-US", "lɜːn", "definition"))));

        var question = service.buildQuestionByWebAPI("learning");

        assertEquals("learning", question.getWord());
        assertTrue(question.isIPAUnavailable(), "Should mark IPA unavailable when dictionary word mismatches request");
        assertNull(question.getPronouncedLink(), "Should skip pronunciationUrl so client falls back to TTS");
        assertArrayEquals(new String[] { "" }, question.getPicsFullPaths());
        verify(webService, never()).searchGoogleImage(anyString(), anyInt());
        verify(phoneticQuestionDAO, never()).insertIfAbsent(any());
    }

    @Test
    void buildQuestionByWebAPI_whenDictionaryMatchesWord_shouldPersistSlimRowWithRankZero() {
        when(webService.queryDictionary("Learning"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(
                        new DictionaryResult("learning", "https://example.com/learning.mp3", "en-US", "ˈlɜːnɪŋ", "definition"))));

        var question = service.buildQuestionByWebAPI("Learning");

        assertEquals("Learning", question.getWord());
        assertFalse(question.isIPAUnavailable());
        assertEquals("ˈlɜːnɪŋ", question.getIPA());
        assertEquals("https://example.com/learning.mp3", question.getPronouncedLink());
        assertEquals(0, question.getRank());
        assertArrayEquals(new String[] { "" }, question.getPicsFullPaths());

        var captor = ArgumentCaptor.forClass(PhoneticQuestion.class);
        verify(phoneticQuestionDAO).insertIfAbsent(captor.capture());
        var saved = captor.getValue();
        assertEquals("Learning", saved.getWord());
        assertEquals("ˈlɜːnɪŋ", saved.getIPA());
        assertEquals("https://example.com/learning.mp3", saved.getPronouncedLink());
        assertEquals(0, saved.getRank());
        assertNull(saved.getPicFileName());
        assertNull(saved.getPronouncedLinkBackup());
        assertTrue(saved.getGrades().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("incompleteDictionaryResults")
    void buildQuestionByWebAPI_whenNotEnriched_shouldNotPersist(Optional<DictionaryResult> result) {
        when(webService.queryDictionary("partial")).thenReturn(CompletableFuture.completedFuture(result));

        var question = service.buildQuestionByWebAPI("partial");

        assertFalse(question.enriched());
        assertArrayEquals(new String[] { "" }, question.getPicsFullPaths());
        verify(phoneticQuestionDAO, never()).insertIfAbsent(any());
    }

    static Stream<Optional<DictionaryResult>> incompleteDictionaryResults() {
        return Stream.of(
                Optional.empty(),
                Optional.of(new DictionaryResult("partial", null, "en-US", "ˈpɑːʃəl", "definition")),
                Optional.of(new DictionaryResult("partial", "   ", "en-US", "ˈpɑːʃəl", "definition")),
                Optional.of(new DictionaryResult("partial", "https://example.com/partial.mp3", "en-US", null, "definition")),
                Optional.of(new DictionaryResult("partial", "https://example.com/partial.mp3", "en-US", "  ", "definition"))
        );
    }
}
