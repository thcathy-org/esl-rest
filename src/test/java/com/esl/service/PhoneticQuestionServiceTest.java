package com.esl.service;

import com.esl.entity.rest.DictionaryResult;
import com.esl.service.rest.WebParserRestService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PhoneticQuestionServiceTest {

    @Test
    void buildQuestionByWebAPI_whenDictionaryResolvesToDifferentWord_shouldSkipPronunciationUrl() {
        // Given
        var webService = mock(WebParserRestService.class);
        var restTemplate = mock(RestTemplate.class);

        // Query is "learning" but dictionary resolves to base form "learn"
        var dictionaryResult = new DictionaryResult(
                "learn",
                "https://example.com/learn.mp3",
                "en-US",
                "lɜːn",
                "definition"
        );
        when(webService.queryDictionary("learning"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(dictionaryResult)));

        var service = new PhoneticQuestionService();
        setField(service, "webService", webService);
        setField(service, "restTemplate", restTemplate);

        // When
        var question = service.buildQuestionByWebAPI("learning", false);

        // Then
        assertEquals("learning", question.getWord());
        assertTrue(question.isIPAUnavailable(), "Should mark IPA unavailable when dictionary word mismatches request");
        assertNull(question.getPronouncedLink(), "Should skip pronunciationUrl so client falls back to TTS");
        verify(webService, never()).searchGoogleImage(anyString(), anyInt());
    }

    @Test
    void buildQuestionByWebAPI_whenDictionaryMatchesWord_shouldUsePronunciationUrl() {
        // Given
        var webService = mock(WebParserRestService.class);
        var restTemplate = mock(RestTemplate.class);

        var dictionaryResult = new DictionaryResult(
                "learning",
                "https://example.com/learning.mp3",
                "en-US",
                "ˈlɜːnɪŋ",
                "definition"
        );
        when(webService.queryDictionary("learning"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(dictionaryResult)));

        var service = new PhoneticQuestionService();
        setField(service, "webService", webService);
        setField(service, "restTemplate", restTemplate);

        // When
        var question = service.buildQuestionByWebAPI("learning", false);

        // Then
        assertEquals("learning", question.getWord());
        assertFalse(question.isIPAUnavailable(), "Should keep IPA available when dictionary provides IPA for exact match");
        assertEquals("ˈlɜːnɪŋ", question.getIPA());
        assertEquals("https://example.com/learning.mp3", question.getPronouncedLink());
    }
}

