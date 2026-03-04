package com.esl.service.tts;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.service.rest.CloudflareAIService;
import com.esl.service.rest.R2StorageService;
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
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class TtsPublisherService {
    private static final Logger logger = LoggerFactory.getLogger(TtsPublisherService.class);
    public static final String PROVIDER_SPEECH_WORKER = "esl_speech_worker";
    public static final String PROVIDER_CLOUDFLARE_AURA2 = "cloudflare_aura2";

    private static final String INVALID_INPUT_DETAIL = "need at least one array to concatenate";
    private static final String INTERNAL_SERVER_ERROR = "500 INTERNAL_SERVER_ERROR";
    private static final List<String> ACTIVE_STATUSES = List.of(
            TtsPublishQueue.STATUS_PENDING,
            TtsPublishQueue.STATUS_FAILED
    );

    private final TransactionTemplate transactionTemplate;
    private final TtsPublishQueueRepository repository;
    private final R2StorageService r2StorageService;
    private final SpeechWorkerService speechWorkerService;
    private final CloudflareAIService cloudflareAIService;

    @Value("${TtsPublisherService.Provider:esl_speech_worker}")
    private String ttsProvider;

    @Value("${TtsPublisherService.Version:v1}")
    private String defaultTtsVersion;

    @Value("${TtsPublisherService.BackoffSeconds}")
    private int backoffSeconds;

    @Value("${TtsPublisherService.MaxAttempts:288}")
    private int maxAttempts;

    @Value("${TtsPublisherService.Voice:af_sarah}")
    private String ttsVoice;

    @Value("${TtsPublisherService.BatchSize:100}")
    private int batchSize;

    public TtsPublisherService(
            TransactionTemplate transactionTemplate,
            TtsPublishQueueRepository repository,
            R2StorageService r2StorageService,
            SpeechWorkerService speechWorkerService,
            CloudflareAIService cloudflareAIService
    ) {
        this.transactionTemplate = transactionTemplate;
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.repository = repository;
        this.r2StorageService = r2StorageService;
        this.speechWorkerService = speechWorkerService;
        this.cloudflareAIService = cloudflareAIService;
    }

    @Scheduled(fixedDelayString = "${TtsPublisherService.IntervalSeconds}", timeUnit = TimeUnit.SECONDS)
    public void publishNext() {
        if (!r2StorageService.isConfigured()) {
            logger.info("TTS publisher skipped: R2 is not configured");
            return;
        }
        if (PROVIDER_CLOUDFLARE_AURA2.equalsIgnoreCase(ttsProvider) && !cloudflareAIService.isConfigured()) {
            logger.warn("TTS publisher skipped: provider={} but Cloudflare AI is not configured", ttsProvider);
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
                        processOne(item);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            } catch (Exception ex) {
                var rootCause = ex.getCause() == null ? ex : ex.getCause();
                if (isInvalidSpeechWorkerInputError(rootCause)) {
                    var itemId = item.getId();
                    if (itemId != null) {
                        repository.deleteById(itemId);
                        logger.warn(
                                "Deleted invalid TTS queue content after non-retryable speech worker error: {}",
                                item.getContent()
                        );
                    } else {
                        logger.warn(
                                "Skipping delete for invalid TTS queue content because id is null: {}",
                                item.getContent()
                        );
                    }
                    continue;
                }
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

    private void processOne(TtsPublishQueue item) throws Exception {
        var ttsVersion = StringUtils.defaultIfBlank(item.getTtsVersion(), defaultTtsVersion);
        var original = StringUtils.trimToNull(item.getContent());
        var normalized = TtsTextUtil.normalize(original);
        var normalKeyHash = TtsTextUtil.sha256Hex(normalized);
        var punctText = TtsTextUtil.normalize(TtsTextUtil.toPunctuationText(normalized));
        var punctKeyHash = TtsTextUtil.sha256Hex(punctText);

        var normalAudioKey = TtsAudioKeyBuilder.buildAudioKey(ttsVersion, normalized, normalKeyHash);
        var punctAudioKey = TtsAudioKeyBuilder.buildAudioKey(ttsVersion, punctText, punctKeyHash);

        var allExist = r2StorageService.exists(normalAudioKey)
                && r2StorageService.exists(punctAudioKey);
        var itemId = Objects.requireNonNull(item.getId(), "TTS queue item id is required");
        if (allExist) {
            repository.deleteById(itemId);
            logger.info("TTS artifacts already exist; deleted queue content: {}", item.getContent());
            return;
        }

        publishVariant(normalized, normalAudioKey);
        publishVariant(punctText, punctAudioKey);
        repository.deleteById(itemId);
        logger.info("Published TTS artifacts for queue content: {}", item.getContent());
    }

    private void publishVariant(String processedText, String audioKey) {
        if (PROVIDER_CLOUDFLARE_AURA2.equalsIgnoreCase(ttsProvider)) {
            publishViaCloudflareTts(processedText, audioKey);
        } else {
            publishViaSpeechWorker(processedText, audioKey);
        }
    }

    private void publishViaSpeechWorker(String processedText, String audioKey) {
        var request = new SpeechWorkerService.GenerateRequest();
        request.text = processedText;
        request.voice = ttsVoice;
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

    private void publishViaCloudflareTts(String processedText, String audioKey) {
        logger.info("Calling Cloudflare AI provider for text={}", processedText);
        var audioBytes = cloudflareAIService.textToSpeech(processedText);
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalStateException("Cloudflare AI returned empty audio");
        }
        r2StorageService.putBytes(audioKey, audioBytes, "audio/mpeg");
    }

    private boolean isInvalidSpeechWorkerInputError(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        var message = StringUtils.defaultString(throwable.getMessage());
        return message.contains(INTERNAL_SERVER_ERROR) && message.contains(INVALID_INPUT_DETAIL);
    }
}
