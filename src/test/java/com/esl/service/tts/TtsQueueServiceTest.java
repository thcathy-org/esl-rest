package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.Vocab;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"null", "unchecked"})
class TtsQueueServiceTest {

    private TtsQueueService createService(TtsPublisherService publisher) {
        return new TtsQueueService(mock(TtsPublishQueueRepository.class), "v1", publisher);
    }

    @Test
    void enqueueForDictation_shouldPublishVocabsWhenPresent() {
        var publisher = mock(TtsPublisherService.class);
        var service = createService(publisher);
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("cat"), new Vocab("dog")));

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(publisher).publishAsync(captor.capture());
        var contents = captor.getValue();
        assertEquals(2, contents.size());
        assertTrue(contents.contains("cat"));
        assertTrue(contents.contains("dog"));
    }

    @Test
    void enqueueForDictation_shouldPublishDeduplicatedArticleChunksAcrossAllPresets() {
        var publisher = mock(TtsPublisherService.class);
        var service = createService(publisher);
        var dictation = new Dictation();
        dictation.setVocabs(List.of());
        dictation.setArticle("The cat sat on the mat. The dog ran fast.");

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(publisher).publishAsync(captor.capture());
        var contents = List.copyOf(captor.getValue());

        assertEquals(List.of(
                "The cat sat",
                "on the mat.",
                "The dog",
                "ran fast.",
                "The dog ran fast.",
                "The cat sat on the mat."
        ), contents);
    }

    @Test
    void enqueueForDictation_shouldPublishSingleChunkWhenArticleDoesNotSplit() {
        var publisher = mock(TtsPublisherService.class);
        var service = createService(publisher);
        var dictation = new Dictation();
        dictation.setVocabs(List.of());
        dictation.setArticle("  Hello world  ");

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(publisher).publishAsync(captor.capture());
        assertEquals(List.of("Hello world"), List.copyOf(captor.getValue()));
    }

    @Test
    void enqueueForDictation_shouldFilterNonQueueableContents() {
        var publisher = mock(TtsPublisherService.class);
        var service = createService(publisher);
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("cat"), new Vocab("。。。"), new Vocab("  ")));

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(publisher).publishAsync(captor.capture());
        assertEquals(List.of("cat"), List.copyOf(captor.getValue()));
    }

    @Test
    void enqueueContent_shouldSkipBlankContent() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = new TtsQueueService(repository, "v1", mock(TtsPublisherService.class));

        service.enqueueContent("   ");

        verify(repository, never()).save(any());
    }

    @Test
    void enqueueContent_shouldSkipWhenMissingEnglishLetterOrDigit() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = new TtsQueueService(repository, "v1", mock(TtsPublisherService.class));

        service.enqueueContent("。。。。");

        verify(repository, never()).save(any());
    }

    @Test
    void enqueueContent_shouldQueueSingleEnglishLetter() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = new TtsQueueService(repository, "v1", mock(TtsPublisherService.class));

        service.enqueueContent("a");

        verify(repository, times(1)).save(any());
    }
}
