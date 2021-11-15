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
        Mockito.when(mockVocabService.createQuestion(any(), anyBoolean())).thenReturn(new PhoneticQuestion());

        service.init();
    }

    @Test
    void createQuestion_willCacheResult() {
        service.createQuestion("word", true);
        service.createQuestion("word", true);
        service.createQuestion("word", false);
        verify(mockVocabService, times(2)).createQuestion(any(), anyBoolean());
    }

    @Test
    void createQuestion_cacheSizeIsLimited() {
        for (int i=0; i<12; i++) {
            service.createQuestion("word" + i, true);
        }
        verify(mockVocabService, times(12)).createQuestion(any(), anyBoolean());
        assertEquals(10, service.cache.estimatedSize());
    }
}
