package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
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

    private TtsQueueService createService(TtsPublishQueueRepository repository) {
        return createService(repository, mock(TtsPublisherService.class), false);
    }

    private TtsQueueService createService(
            TtsPublishQueueRepository repository,
            TtsPublisherService publisher,
            boolean vocabPublishAsync
    ) {
        return new TtsQueueService(repository, "v1", vocabPublishAsync, publisher);
    }

    @Test
    void enqueueForDictation_shouldQueueVocabsWhenPresent() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("cat"), new Vocab("dog")));

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(TtsPublishQueue.class);
        verify(repository, times(2)).save(captor.capture());
        var contents = captor.getAllValues().stream().map(TtsPublishQueue::getContent).toList();
        assertEquals(2, contents.size());
        assertTrue(contents.contains("cat"));
        assertTrue(contents.contains("dog"));
    }

    @Test
    void enqueueForDictation_shouldPublishVocabsWhenVocabPublishAsyncEnabled() {
        var publisher = mock(TtsPublisherService.class);
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository, publisher, true);
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("cat"), new Vocab("dog")));

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(publisher).publishAsync(captor.capture());
        var contents = captor.getValue();
        assertEquals(2, contents.size());
        assertTrue(contents.contains("cat"));
        assertTrue(contents.contains("dog"));
        verify(repository, never()).save(any());
    }

    @Test
    void enqueueForDictation_shouldQueueDeduplicatedArticleChunksAcrossAllPresets() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);
        var dictation = new Dictation();
        dictation.setVocabs(List.of());
        dictation.setArticle("The cat sat on the mat. The dog ran fast.");

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(TtsPublishQueue.class);
        verify(repository, times(6)).save(captor.capture());
        var contents = captor.getAllValues().stream().map(TtsPublishQueue::getContent).toList();
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
    void enqueueForDictation_shouldQueueSingleChunkWhenArticleDoesNotSplit() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);
        var dictation = new Dictation();
        dictation.setVocabs(List.of());
        dictation.setArticle("  Hello world  ");

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(TtsPublishQueue.class);
        verify(repository).save(captor.capture());
        assertEquals("Hello world", captor.getValue().getContent());
    }

    @Test
    void enqueueForDictation_shouldFilterNonQueueableContents() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("cat"), new Vocab("。。。"), new Vocab("  ")));

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(TtsPublishQueue.class);
        verify(repository).save(captor.capture());
        assertEquals("cat", captor.getValue().getContent());
    }

    @Test
    void enqueueForDictation_shouldFilterNonQueueableContentsWhenVocabPublishAsyncEnabled() {
        var publisher = mock(TtsPublisherService.class);
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository, publisher, true);
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("cat"), new Vocab("。。。"), new Vocab("  ")));

        service.enqueueForDictation(dictation);

        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(publisher).publishAsync(captor.capture());
        assertEquals(List.of("cat"), List.copyOf(captor.getValue()));
    }

    @Test
    void enqueueForDictation_shouldQueueAllArticleChunksRegardlessOfLength() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);
        var dictation = new Dictation();
        dictation.setVocabs(List.of());
        dictation.setArticle("The cat sat on the mat. The dog ran fast.");

        service.enqueueForDictation(dictation);

        verify(repository, atLeastOnce()).save(any());
    }

    @Test
    void enqueueForDictation_shouldQueueShortArticles() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);
        var dictation = new Dictation();
        dictation.setVocabs(List.of());
        dictation.setArticle("Hello world");

        service.enqueueForDictation(dictation);

        verify(repository).save(any());
    }

    @Test
    void enqueueForDictation_shouldQueueVocabWords() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);
        var dictation = new Dictation();
        dictation.setVocabs(List.of(new Vocab("elephant"), new Vocab("kangaroo")));

        service.enqueueForDictation(dictation);

        verify(repository, times(2)).save(any());
    }

    @Test
    void enqueueContent_shouldSkipBlankContent() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);

        service.enqueueContent("   ");

        verify(repository, never()).save(any());
    }

    @Test
    void enqueueContent_shouldSkipWhenMissingEnglishLetterOrDigit() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);

        service.enqueueContent("。。。。");

        verify(repository, never()).save(any());
    }

    @Test
    void enqueueContent_shouldQueueSingleEnglishLetter() {
        var repository = mock(TtsPublishQueueRepository.class);
        var service = createService(repository);

        service.enqueueContent("a");

        verify(repository, times(1)).save(any());
    }
}
