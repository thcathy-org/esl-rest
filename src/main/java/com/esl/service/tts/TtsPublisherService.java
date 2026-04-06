package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.service.rest.CloudflareAIService;
import com.esl.service.rest.R2StorageService;
import com.esl.service.rest.ReplicateAIService;
import com.esl.service.rest.SpeechWorkerService;
import com.esl.util.TtsTextUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TtsPublisherService {
    private static final Logger logger = LoggerFactory.getLogger(TtsPublisherService.class);
    public static final String PROVIDER_SPEECH_WORKER = "esl_speech_worker";
    public static final String PROVIDER_CLOUDFLARE_AURA2 = "cloudflare_aura2";
    public static final String PROVIDER_INWORLD_TTS = "inworld_tts";

    private static final List<String> ACTIVE_STATUSES = List.of(TtsPublishQueue.STATUS_FAILED);

    private final TransactionTemplate transactionTemplate;
    private final TtsPublishQueueRepository repository;
    private final R2StorageService r2StorageService;
    private final SpeechWorkerService speechWorkerService;
    private final CloudflareAIService cloudflareAIService;
    private final ReplicateAIService replicateAIService;
    private final ExecutorService executionPool;

    @Value("${TtsPublisherService.Provider:esl_speech_worker}")
    private String ttsProvider;

    @Value("${TtsPublisherService.Version:v1}")
    private String defaultTtsVersion;

    @Value("${TtsPublisherService.BackoffSeconds}")
    private int backoffSeconds;

    @Value("${TtsPublisherService.MaxAttempts:288}")
    private int maxAttempts;

    @Value("${TtsPublisherService.Voice:}")
    private String ttsVoice;

    @Value("${TtsPublisherService.BatchSize:100}")
    private int batchSize;

    public TtsPublisherService(
            TransactionTemplate transactionTemplate,
            TtsPublishQueueRepository repository,
            R2StorageService r2StorageService,
            SpeechWorkerService speechWorkerService,
            CloudflareAIService cloudflareAIService,
            ReplicateAIService replicateAIService,
            ExecutorService executionPool
    ) {
        this.transactionTemplate = transactionTemplate;
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.repository = repository;
        this.r2StorageService = r2StorageService;
        this.speechWorkerService = speechWorkerService;
        this.cloudflareAIService = cloudflareAIService;
        this.replicateAIService = replicateAIService;
        this.executionPool = executionPool;
    }

    public void publishAsync(Collection<String> contents) {
        if (!r2StorageService.isConfigured()) {
            logger.info("publishAsync skipped: R2 is not configured");
            return;
        }
        if (!isProviderConfigured()) {
            logger.warn("publishAsync skipped: provider={} is not configured", ttsProvider);
            return;
        }

        contents.forEach(content ->
                CompletableFuture.runAsync(() -> publishOne(content), executionPool));
    }

    private void publishOne(String content) {
        try {
            processContentString(content, false);
        } catch (Exception ex) {
            logger.error("publishAsync: TTS failed, saving FAILED queue entry for: {}", content, ex);
            saveFailedQueueEntry(content, ex);
        }
    }

    @Scheduled(fixedDelayString = "${TtsPublisherService.IntervalSeconds}", timeUnit = TimeUnit.SECONDS)
    public void publishNext() {
        if (!r2StorageService.isConfigured()) {
            logger.info("TTS publisher skipped: R2 is not configured");
            return;
        }
        if (!isProviderConfigured()) {
            logger.warn("TTS publisher skipped: provider={} is not configured", ttsProvider);
            return;
        }

        var now = new Date();
        var page = PageRequest.of(0, batchSize);
        var results = repository.findNext(ACTIVE_STATUSES, now, maxAttempts, page);
        if (results.isEmpty()) {
            return;
        }

        for (var item : results) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    try {
                        var itemId = Objects.requireNonNull(item.getId(), "TTS queue item id is required");
                        processContentString(item.getContent(), item.isForceReplaceAudio());
                        repository.deleteById(itemId);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } catch (Exception ex) {
                var rootCause = ex.getCause() == null ? ex : ex.getCause();
                logger.error("Publishing TTS artifacts for queue content {}", item.getContent(), rootCause);
                var nextAttemptAt = Date.from(Instant.now().plus(backoffSeconds, ChronoUnit.SECONDS));
                item.setAttemptCount(item.getAttemptCount() + 1);
                item.setNextAttemptAt(nextAttemptAt);
                item.setLastUpdatedDate(now);
                item.setStatus(TtsPublishQueue.STATUS_FAILED);
                item.setLastError(StringUtils.abbreviate(rootCause.getMessage(), 1000));
                repository.save(item);
                return;
            }
        }
    }

    private void processContentString(String content, boolean forceReplaceAudio) {
        var ttsVersion = defaultTtsVersion;
        var normalized = TtsTextUtil.normalize(StringUtils.trimToNull(content));
        var normalKeyHash = TtsTextUtil.sha256Hex(normalized);
        var punctText = TtsTextUtil.normalize(TtsTextUtil.toPunctuationText(normalized));
        var punctKeyHash = TtsTextUtil.sha256Hex(punctText);

        var normalAudioKey = TtsAudioKeyBuilder.buildAudioKey(ttsVersion, normalized, normalKeyHash);
        var punctAudioKey = TtsAudioKeyBuilder.buildAudioKey(ttsVersion, punctText, punctKeyHash);

        if (!forceReplaceAudio) {
            var allExist = r2StorageService.exists(normalAudioKey) && r2StorageService.exists(punctAudioKey);
            if (allExist) {
                logger.info("processContentString: content already exists: {}", content);
                return;
            }
        }

        publishVariant(normalized, normalAudioKey);
        publishVariant(punctText, punctAudioKey);
        logger.info("processContentString: TTS published for content: {}", content);
    }

    private void saveFailedQueueEntry(String content, Throwable cause) {
        var now = new Date();
        var item = new TtsPublishQueue();
        item.setTtsVersion(defaultTtsVersion);
        item.setContent(content);
        item.setStatus(TtsPublishQueue.STATUS_FAILED);
        item.setAttemptCount(1);
        item.setCreatedDate(now);
        item.setLastUpdatedDate(now);
        item.setNextAttemptAt(Date.from(Instant.now().plus(backoffSeconds, ChronoUnit.SECONDS)));
        item.setLastError(StringUtils.abbreviate(cause.getMessage(), 1000));
        repository.save(item);
    }

    private boolean isProviderConfigured() {
        if (PROVIDER_CLOUDFLARE_AURA2.equalsIgnoreCase(ttsProvider)) return cloudflareAIService.isConfigured();
        if (PROVIDER_INWORLD_TTS.equalsIgnoreCase(ttsProvider)) return replicateAIService.isConfigured();
        return true;
    }

    private void publishVariant(String processedText, String audioKey) {
        if (PROVIDER_CLOUDFLARE_AURA2.equalsIgnoreCase(ttsProvider)) {
            publishViaCloudflareTts(processedText, audioKey);
        } else if (PROVIDER_INWORLD_TTS.equalsIgnoreCase(ttsProvider)) {
            publishViaInworldTts(processedText, audioKey);
        } else {
            publishViaSpeechWorker(processedText, audioKey);
        }
    }

    private void publishViaSpeechWorker(String processedText, String audioKey) {
        var request = new SpeechWorkerService.GenerateRequest();
        request.text = processedText;
        if (StringUtils.isNotBlank(ttsVoice)) request.voice = ttsVoice;
        request.audioFormat = "mp3";

        logger.info("Calling speech worker provider for text={}", processedText);
        var response = speechWorkerService.generate(request);
        if (response == null || StringUtils.isBlank(response.audioBase64)) {
            throw new IllegalStateException("Speech worker returned empty audio");
        }

        var audioBytes = Base64.getDecoder().decode(response.audioBase64);
        var mimeType = StringUtils.defaultIfBlank(response.mimeType, "audio/mpeg");
        r2StorageService.putBytes(audioKey, audioBytes, mimeType);
    }

    private void publishViaInworldTts(String processedText, String audioKey) {
        logger.info("Calling Inworld TTS provider for text={}", processedText);
        var audioBytes = replicateAIService.inworldTextToSpeech(processedText);
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalStateException("Inworld TTS returned empty audio");
        }
        r2StorageService.putBytes(audioKey, audioBytes, "audio/mpeg");
    }

    private void publishViaCloudflareTts(String processedText, String audioKey) {
        logger.info("Calling Cloudflare AI provider for text={}", processedText);
        var audioBytes = cloudflareAIService.textToSpeech(processedText);
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalStateException("Cloudflare AI returned empty audio");
        }
        r2StorageService.putBytes(audioKey, audioBytes, "audio/mpeg");
    }

}
