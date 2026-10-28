package com.esl.service;

import com.esl.dao.repository.PracticeHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class PracticeHistoryRetentionJob {
    private static final String DAILY_CRON = "0 15 3 * * *";

    private static final Logger logger = LoggerFactory.getLogger(PracticeHistoryRetentionJob.class);

    private final PracticeHistoryRepository repository;
    private final int retentionMonths;
    private final int batchSize;
    private final boolean enabled;

    @Autowired
    public PracticeHistoryRetentionJob(
            PracticeHistoryRepository repository,
            @Value("${PracticeHistory.RetentionMonths:12}") int retentionMonths,
            @Value("${PracticeHistory.RetentionBatchSize:500}") int batchSize,
            @Value("${PracticeHistory.RetentionEnabled:false}") boolean enabled
    ) {
        this.repository = repository;
        this.retentionMonths = retentionMonths;
        this.batchSize = batchSize;
        this.enabled = enabled;
    }

    static Date cutoff(Instant now, int retentionMonths) {
        if (retentionMonths < 1) {
            throw new IllegalArgumentException("retentionMonths must be at least 1");
        }
        return Date.from(now.atOffset(ZoneOffset.UTC).minusMonths(retentionMonths).toInstant());
    }

    @Scheduled(cron = DAILY_CRON, zone = "UTC")
    public void run() {
        if (retentionMonths < 1 || batchSize < 1) {
            logger.error(
                    "Practice history retention skipped: retentionMonths={} batchSize={}",
                    retentionMonths,
                    batchSize
            );
            return;
        }

        var cutoff = cutoff(Instant.now(), retentionMonths);
        if (!enabled) {
            var matched = repository.countByCreatedDateBefore(cutoff);
            logger.info(
                    "Practice history retention dry-run: {} rows older than {} would be deleted. PracticeHistory.RetentionEnabled=false",
                    matched,
                    cutoff
            );
            return;
        }

        var deleted = deleteOlderThan(cutoff);
        logger.info("Practice history retention deleted {} rows older than {}", deleted, cutoff);
    }

    private long deleteOlderThan(Date cutoff) {
        var page = PageRequest.of(0, batchSize, Sort.by(Sort.Direction.ASC, "id"));
        long deletedTotal = 0;
        while (true) {
            var ids = repository.findIdsByCreatedDateBefore(cutoff, page);
            if (ids.isEmpty()) {
                return deletedTotal;
            }
            var deleted = repository.deleteByIdIn(ids);
            if (deleted <= 0) {
                logger.error(
                        "Practice history retention stopped: selected {} ids but deleted {}",
                        ids.size(),
                        deleted
                );
                return deletedTotal;
            }
            deletedTotal += deleted;
            if (ids.size() < batchSize) {
                return deletedTotal;
            }
        }
    }
}
