package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.Vocab;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TtsQueueServiceTest {

    @Test
    void enqueueForDictation_shouldQueueVocabsWhenPresent() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = new TtsQueueService(repository, "v1");
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("cat"), new Vocab("dog")));

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(TtsPublishQueue.class);
        verify(repository, times(2)).save(captor.capture());
        var contents = captor.getAllValues().stream()
                .map(TtsPublishQueue::getContent)
                .toList();

        assertEquals(2, contents.size());
        assertTrue(contents.contains("cat"));
        assertTrue(contents.contains("dog"));
    }

    @Test
    void enqueueForDictation_shouldQueueArticleWhenNoVocabs() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = new TtsQueueService(repository, "v1");
        var dictation = new Dictation();
        dictation.setVocabs(List.of());
        dictation.setArticle("  Hello world  ");

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(TtsPublishQueue.class);
        verify(repository).save(captor.capture());
        assertEquals("Hello world", captor.getValue().getContent());
    }

    @Test
    void enqueueContent_shouldSkipBlankContent() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = new TtsQueueService(repository, "v1");

        service.enqueueContent("   ");

        verify(repository, never()).save(any());
    }
}
