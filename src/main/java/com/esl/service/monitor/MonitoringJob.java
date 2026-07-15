package com.esl.service.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MonitoringJob {
    private static final Logger logger = LoggerFactory.getLogger(MonitoringJob.class);

    private final List<Monitor> monitors;
    private final boolean enabled;

    public MonitoringJob(
            List<Monitor> monitors,
            @Value("${Monitoring.Enabled:true}") boolean enabled
    ) {
        this.monitors = monitors;
        this.enabled = enabled;
    }

    @Scheduled(fixedRateString = "${Monitoring.IntervalSeconds:3600}", timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!enabled) {
            return;
        }
        for (var monitor : monitors) {
            try {
                monitor.run();
            } catch (Exception ex) {
                logger.error("Monitor {} failed", monitor.getClass().getSimpleName(), ex);
            }
        }
    }
}
