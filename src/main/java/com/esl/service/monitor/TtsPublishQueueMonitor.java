package com.esl.service.monitor;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.service.rest.TelegramNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

@Service
public class TtsPublishQueueMonitor implements Monitor {
    private static final Logger logger = LoggerFactory.getLogger(TtsPublishQueueMonitor.class);

    private final TtsPublishQueueRepository repository;
    private final TelegramNotificationService telegramNotificationService;
    private final int maxAttempts;
    private final boolean enabled;
    private final boolean alertEnabled;
    private final long activeCountThreshold;
    private final long oldestItemMinutesThreshold;

    public TtsPublishQueueMonitor(
            TtsPublishQueueRepository repository,
            TelegramNotificationService telegramNotificationService,
            @Value("${TtsPublisherService.MaxAttempts:288}") int maxAttempts,
            @Value("${TtsPublishQueueMonitor.Enabled:true}") boolean enabled,
            @Value("${TtsPublishQueueMonitor.AlertEnabled:false}") boolean alertEnabled,
            @Value("${TtsPublishQueueMonitor.ActiveCountThreshold:100}") long activeCountThreshold,
            @Value("${TtsPublishQueueMonitor.OldestItemMinutesThreshold:60}") long oldestItemMinutesThreshold
    ) {
        this.repository = repository;
        this.telegramNotificationService = telegramNotificationService;
        this.maxAttempts = maxAttempts;
        this.enabled = enabled;
        this.alertEnabled = alertEnabled;
        this.activeCountThreshold = activeCountThreshold;
        this.oldestItemMinutesThreshold = oldestItemMinutesThreshold;
    }

    @Override
    public void run() {
        if (!enabled) {
            return;
        }

        var now = new Date();
        var oldestCreated = repository.findOldestActiveCreatedDate(maxAttempts).orElse(null);
        var activeCount = repository.countActive(maxAttempts);
        var readyNowCount = repository.countReadyNow(now, maxAttempts);
        var pendingCount = repository.countByStatus(TtsPublishQueue.STATUS_PENDING);
        var failedCount = repository.countByStatus(TtsPublishQueue.STATUS_FAILED);
        var oldestItemAgeMinutes = toAgeMinutes(oldestCreated, now);

        logger.info(
                "TTS publish queue: active={}, readyNow={}, pending={}, failed={}, oldestMin={}",
                activeCount,
                readyNowCount,
                pendingCount,
                failedCount,
                oldestItemAgeMinutes
        );
        maybeAlert(activeCount, oldestItemAgeMinutes);
    }

    private void maybeAlert(long activeCount, Long oldestItemAgeMinutes) {
        if (!alertEnabled) {
            return;
        }
        if (!telegramNotificationService.isConfigured()) {
            logger.debug("TTS queue alert skipped: Telegram is not configured");
            return;
        }

        var reasons = new ArrayList<String>();
        if (activeCount >= activeCountThreshold) {
            reasons.add("active=" + activeCount + " (threshold " + activeCountThreshold + ")");
        }
        if (oldestItemAgeMinutes != null && oldestItemAgeMinutes >= oldestItemMinutesThreshold) {
            reasons.add("oldest=" + oldestItemAgeMinutes + "min (threshold " + oldestItemMinutesThreshold + "min)");
        }
        if (reasons.isEmpty()) {
            return;
        }

        var message = "TTS publish queue alert: " + String.join(", ", reasons);
        telegramNotificationService.sendMessage(message);
        logger.warn("TTS queue alert sent: {}", message);
    }

    private static Long toAgeMinutes(Date createdDate, Date now) {
        if (createdDate == null) {
            return null;
        }
        return Duration.between(createdDate.toInstant(), now.toInstant()).toMinutes();
    }
}
