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
    private final TtsPublisherService ttsPublisherService;

    public TtsQueueService(
            TtsPublishQueueRepository repository,
            @Value("${TtsPublisherService.Version:v1}") String ttsVersion,
            TtsPublisherService ttsPublisherService
    ) {
        this.repository = repository;
        this.ttsVersion = ttsVersion;
        this.ttsPublisherService = ttsPublisherService;
    }

    public void enqueueForDictation(Dictation dictation) {
        ttsPublisherService.publishAsync(collectContents(dictation));
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
        var strings = (vocabs != null && !vocabs.isEmpty())
                ? vocabs.stream().map(v -> StringUtils.trimToNull(v.getWord()))
                : articleChunks(dictation.getArticle());
        return strings.filter(this::isQueueableContent).toList();
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
