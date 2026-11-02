package com.esl.service;

import com.esl.model.PhoneticQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CachedVocabServiceTest {

    CachedVocabService service;
    VocabService mockVocabService;

    @BeforeEach
    public void setup() {
        service = new CachedVocabService();
        service.cacheSize = 10;

        mockVocabService = mock(VocabService.class);
        service.vocabService = mockVocabService;
        Mockito.when(mockVocabService.createQuestion(any())).thenReturn(new PhoneticQuestion());

        service.init();
    }

    @Test
    void createQuestion_willCacheResult() {
        service.createQuestion("word");
        service.createQuestion("word");
        verify(mockVocabService, times(1)).createQuestion(any());
    }

    @Test
    void createQuestion_cacheSizeIsLimited() {
        for (int i=0; i<12; i++) {
            service.createQuestion("word" + i);
        }
        verify(mockVocabService, times(12)).createQuestion(any());
        service.cache.cleanUp();
        assertEquals(10, service.cache.estimatedSize());
    }
}
