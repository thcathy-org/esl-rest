package com.esl.service.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringJobTest {

    @Mock Monitor firstMonitor;
    @Mock Monitor secondMonitor;

    MonitoringJob job;

    @BeforeEach
    void setUp() {
        job = new MonitoringJob(List.of(firstMonitor, secondMonitor), true);
    }

    @Test
    void run_shouldExecuteAllMonitors() {
        job.run();

        verify(firstMonitor).run();
        verify(secondMonitor).run();
    }

    @Test
    void run_shouldContinueWhenEarlierMonitorFails() {
        doThrow(new RuntimeException("boom")).when(firstMonitor).run();

        job.run();

        verify(firstMonitor).run();
        verify(secondMonitor).run();
    }

    @Test
    void run_shouldSkipWhenDisabled() {
        var disabledJob = new MonitoringJob(List.of(firstMonitor, secondMonitor), false);

        disabledJob.run();

        verifyNoInteractions(firstMonitor, secondMonitor);
    }
}
