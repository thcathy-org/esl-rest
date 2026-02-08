package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.entity.dictation.Dictation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TtsQueueService {
    private final TtsPublishQueueRepository repository;
    private final String ttsVersion;

    public TtsQueueService(
            TtsPublishQueueRepository repository,
            @Value("${TtsPublisherService.Version:v1}") String ttsVersion
    ) {
        this.repository = repository;
        this.ttsVersion = ttsVersion;
    }

    public void enqueueForDictation(Dictation dictation) {
        if (dictation == null) {
            return;
        }

        var vocabs = dictation.getVocabs();
        if (vocabs != null && !vocabs.isEmpty()) {
            for (var vocab : vocabs) {
                enqueueContent(vocab.getWord());
            }
            return;
        }

        var article = StringUtils.trimToNull(dictation.getArticle());
        if (article != null) {
            enqueueContent(article);
        }
    }

    public void enqueueContent(String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }

        var now = new Date();
        var item = new TtsPublishQueue();
        item.setTtsVersion(ttsVersion);
        item.setStatus(TtsPublishQueue.STATUS_PENDING);
        item.setAttemptCount(0);
        item.setCreatedDate(now);
        item.setLastUpdatedDate(now);
        item.setContent(content);

        repository.save(item);
    }
}
