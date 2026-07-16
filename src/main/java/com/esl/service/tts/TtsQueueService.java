package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.entity.dictation.Dictation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class TtsQueueService {
    private static final Pattern ENGLISH_OR_DIGIT_PATTERN = Pattern.compile(".*[A-Za-z0-9].*");
    private final TtsPublishQueueRepository repository;
    private final String ttsVersion;
    private final boolean vocabPublishAsync;
    private final TtsPublisherService ttsPublisherService;

    public TtsQueueService(
            TtsPublishQueueRepository repository,
            @Value("${TtsPublisherService.Version:v1}") String ttsVersion,
            @Value("${TtsQueueService.VocabPublishAsync:false}") boolean vocabPublishAsync,
            TtsPublisherService ttsPublisherService
    ) {
        this.repository = repository;
        this.ttsVersion = ttsVersion;
        this.vocabPublishAsync = vocabPublishAsync;
        this.ttsPublisherService = ttsPublisherService;
    }

    public void enqueueForDictation(Dictation dictation) {
        var contents = collectContents(dictation);
        if (vocabPublishAsync && isVocabDictation(dictation)) {
            ttsPublisherService.publishAsync(contents);
        } else {
            contents.forEach(this::enqueueContent);
        }
    }

    private boolean isVocabDictation(Dictation dictation) {
        var vocabs = dictation.getVocabs();
        return vocabs != null && !vocabs.isEmpty();
    }

    public void enqueueContent(String content) {
        var trimmed = StringUtils.trimToNull(content);
        if (!isQueueableContent(trimmed)) {
            return;
        }

        var now = new Date();
        var item = new TtsPublishQueue();
        item.setTtsVersion(ttsVersion);
        item.setStatus(TtsPublishQueue.STATUS_PENDING);
        item.setAttemptCount(0);
        item.setCreatedDate(now);
        item.setLastUpdatedDate(now);
        item.setContent(trimmed);

        repository.save(item);
    }

    private List<String> collectContents(Dictation dictation) {
        var vocabs = dictation.getVocabs();
        var strings = isVocabDictation(dictation)
                ? vocabs.stream().map(v -> StringUtils.trimToNull(v.getWord()))
                : articleChunks(dictation.getArticle());
        return strings.filter(this::isQueueableContent).distinct().toList();
    }

    private Stream<String> articleChunks(String article) {
        var trimmed = StringUtils.trimToNull(article);
        if (trimmed == null) return Stream.empty();
        return DictationSentenceChunker.ALL_PRESET_WORD_COUNTS.stream()
                .flatMap(maxWords -> DictationSentenceChunker.divideToSentences(trimmed, maxWords).stream());
    }

    boolean isQueueableContent(String trimmedContent) {
        if (trimmedContent == null) {
            return false;
        }
        return ENGLISH_OR_DIGIT_PATTERN.matcher(trimmedContent).matches();
    }
}
