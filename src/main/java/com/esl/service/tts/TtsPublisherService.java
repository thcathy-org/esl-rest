package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.service.rest.R2StorageService;
import com.esl.service.rest.SpeechWorkerService;
import com.esl.util.TtsTextUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TtsPublisherService {
    private static final Logger logger = LoggerFactory.getLogger(TtsPublisherService.class);
    private static final List<String> ACTIVE_STATUSES = List.of(
            TtsPublishQueue.STATUS_PENDING,
            TtsPublishQueue.STATUS_FAILED
    );

    private final TtsPublishQueueRepository repository;
    private final R2StorageService r2StorageService;
    private final SpeechWorkerService speechWorkerService;
    private final ObjectMapper objectMapper;
    @Value("${TtsPublisherService.Version:v1}")
    private String defaultTtsVersion;

    @Value("${TtsPublisherService.BackoffSeconds}")
    private int backoffSeconds;

    @Value("${TtsPublisherService.Voice:af_sarah}")
    private String ttsVoice;

    @Value("${TtsPublisherService.BatchSize:100}")
    private int batchSize;

    public TtsPublisherService(
            TtsPublishQueueRepository repository,
            R2StorageService r2StorageService,
            SpeechWorkerService speechWorkerService,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.r2StorageService = r2StorageService;
        this.speechWorkerService = speechWorkerService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${TtsPublisherService.IntervalSeconds}", timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void publishNext() {
        if (!r2StorageService.isConfigured()) {
            logger.info("TTS publisher skipped: R2 is not configured");
            return;
        }

        var now = new Date();
        var page = PageRequest.of(0, batchSize);
        var results = repository.findNext(ACTIVE_STATUSES, now, page);
        if (results.isEmpty()) {
            return;
        }

        for (var item : results) {
            try {
                var ttsVersion = StringUtils.defaultIfBlank(item.getTtsVersion(), defaultTtsVersion);
                var original = StringUtils.trimToNull(item.getContent());
                var normalized = TtsTextUtil.normalize(original);
                var normalKeyHash = TtsTextUtil.sha256Hex(normalized);
                var punctText = TtsTextUtil.normalize(TtsTextUtil.toPunctuationText(normalized));
                var punctKeyHash = TtsTextUtil.sha256Hex(punctText);

                var normalAudioKey = buildAudioKey(ttsVersion, normalKeyHash);
                var normalTimestampsKey = buildTimestampsKey(ttsVersion, normalKeyHash);
                var punctAudioKey = buildAudioKey(ttsVersion, punctKeyHash);
                var punctTimestampsKey = buildTimestampsKey(ttsVersion, punctKeyHash);

                var allExist = r2StorageService.exists(normalAudioKey)
                        && r2StorageService.exists(normalTimestampsKey)
                        && r2StorageService.exists(punctAudioKey)
                        && r2StorageService.exists(punctTimestampsKey);
                if (allExist) {
                    repository.delete(item);
                    logger.info("TTS artifacts already exist; deleted queue content: {}", item.getContent());
                    continue;
                }

                publishVariant(normalized, normalAudioKey, normalTimestampsKey);
                publishVariant(punctText, punctAudioKey, punctTimestampsKey);
                repository.delete(item);
                logger.info("Published TTS artifacts for queue content: {}", item.getContent());
            } catch (Exception ex) {
                logger.error("Publishing TTS artifacts for queue content {}", item.getContent(), ex);
                var nextAttemptAt = Date.from(Instant.now().plus(backoffSeconds, ChronoUnit.SECONDS));
                item.setAttemptCount(item.getAttemptCount() + 1);
                item.setNextAttemptAt(nextAttemptAt);
                item.setLastUpdatedDate(now);
                item.setStatus(TtsPublishQueue.STATUS_FAILED);
                item.setLastError(StringUtils.abbreviate(ex.getMessage(), 1000));
                repository.save(item);
                return;
            }
        }
    }

    private String buildAudioKey(String ttsVersion, String keyHash) {
        var shard = keyHash.substring(0, 2);
        return String.format("tts/%s/%s/%s/audio.mp3", ttsVersion, shard, keyHash);
    }

    private String buildTimestampsKey(String ttsVersion, String keyHash) {
        var shard = keyHash.substring(0, 2);
        return String.format("tts/%s/%s/%s/timestamps.json", ttsVersion, shard, keyHash);
    }

    private void publishVariant(String processedText, String audioKey, String timestampsKey) throws Exception {
        var request = new SpeechWorkerService.GenerateRequest();
        request.text = processedText;
        request.voice = ttsVoice;
        request.audioFormat = "mp3";

        var response = speechWorkerService.generate(request);
        if (response == null || StringUtils.isBlank(response.audioBase64)) {
            throw new IllegalStateException("Speech worker returned empty audio");
        }

        var audioBytes = Base64.getDecoder().decode(response.audioBase64);
        var mimeType = StringUtils.defaultIfBlank(response.mimeType, "audio/mpeg");
        r2StorageService.putBytes(audioKey, audioBytes, mimeType);

        var timestampsPayload = new java.util.HashMap<String, Object>();
        timestampsPayload.put("word_timestamps", response.wordTimestamps);
        timestampsPayload.put("original_text", response.originalText);
        timestampsPayload.put("processed_text", response.processedText);
        timestampsPayload.put("sample_rate", response.sampleRate);
        var timestampsBytes = objectMapper.writeValueAsBytes(timestampsPayload);
        r2StorageService.putBytes(timestampsKey, timestampsBytes, "application/json");
    }
}
