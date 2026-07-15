package com.esl.service.monitor;

import com.esl.dao.repository.TtsPublishQueueRepository;
import com.esl.entity.TtsPublishQueue;
import com.esl.service.rest.TelegramNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TtsPublishQueueMonitorTest {

    @Mock TtsPublishQueueRepository repository;
    @Mock TelegramNotificationService telegramNotificationService;

    TtsPublishQueueMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new TtsPublishQueueMonitor(
                repository,
                telegramNotificationService,
                288,
                true,
                true,
                100,
                60
        );
    }

    @Test
    void run_shouldCollectMetricsWithoutAlertWhenHealthy() {
        stubMetrics(5L, 3L, 4L, 1L, 10L);

        monitor.run();

        verify(telegramNotificationService, never()).sendMessage(anyString());
    }

    @Test
    void run_shouldSkipWhenDisabled() {
        var disabledMonitor = new TtsPublishQueueMonitor(
                repository,
                telegramNotificationService,
                288,
                false,
                true,
                100,
                60
        );

        disabledMonitor.run();

        verifyNoInteractions(repository, telegramNotificationService);
    }

    @Test
    void run_shouldAlertWhenActiveCountExceedsThreshold() {
        stubMetrics(150L, 120L, 140L, 10L, 5L);
        when(telegramNotificationService.isConfigured()).thenReturn(true);

        monitor.run();

        verify(telegramNotificationService).sendMessage(
                "TTS publish queue alert: active=150 (threshold 100)"
        );
    }

    @Test
    void run_shouldAlertWhenActiveCountAtThreshold() {
        stubMetrics(100L, 80L, 90L, 10L, 5L);
        when(telegramNotificationService.isConfigured()).thenReturn(true);

        monitor.run();

        verify(telegramNotificationService).sendMessage(
                "TTS publish queue alert: active=100 (threshold 100)"
        );
    }

    @Test
    void run_shouldAlertWhenOldestItemAtThreshold() {
        stubMetrics(10L, 10L, 10L, 0L, 60L);
        when(telegramNotificationService.isConfigured()).thenReturn(true);

        monitor.run();

        verify(telegramNotificationService).sendMessage(
                "TTS publish queue alert: oldest=60min (threshold 60min)"
        );
    }

    @Test
    void run_shouldAlertWhenOldestItemExceedsThreshold() {
        stubMetrics(10L, 10L, 10L, 0L, 90L);
        when(telegramNotificationService.isConfigured()).thenReturn(true);

        monitor.run();

        verify(telegramNotificationService).sendMessage(
                "TTS publish queue alert: oldest=90min (threshold 60min)"
        );
    }

    @Test
    void run_shouldSkipAlertWhenTelegramNotConfigured() {
        stubMetrics(150L, 120L, 140L, 10L, 5L);
        when(telegramNotificationService.isConfigured()).thenReturn(false);

        monitor.run();

        verify(telegramNotificationService, never()).sendMessage(anyString());
    }

    @Test
    void run_shouldSkipAlertWhenAlertDisabled() {
        var monitorWithoutAlerts = new TtsPublishQueueMonitor(
                repository,
                telegramNotificationService,
                288,
                true,
                false,
                100,
                60
        );
        stubMetrics(150L, 120L, 140L, 10L, 5L);

        monitorWithoutAlerts.run();

        verify(telegramNotificationService, never()).sendMessage(anyString());
    }

    private void stubMetrics(long active, long readyNow, long pending, long failed, long oldestAgeMinutes) {
        var now = new Date();
        var oldest = Date.from(now.toInstant().minusSeconds(oldestAgeMinutes * 60));

        when(repository.countActive(288)).thenReturn(active);
        when(repository.countReadyNow(any(Date.class), eq(288))).thenReturn(readyNow);
        when(repository.countByStatus(TtsPublishQueue.STATUS_PENDING)).thenReturn(pending);
        when(repository.countByStatus(TtsPublishQueue.STATUS_FAILED)).thenReturn(failed);
        when(repository.findOldestActiveCreatedDate(288)).thenReturn(Optional.of(oldest));
    }
}
