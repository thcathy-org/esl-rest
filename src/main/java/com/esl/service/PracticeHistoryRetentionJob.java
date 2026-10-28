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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PracticeHistoryRetentionJob {
    private static final Logger logger = LoggerFactory.getLogger(PracticeHistoryRetentionJob.class);

    private final PracticeHistoryRepository repository;
    private final Clock clock;
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
        this(repository, Clock.systemUTC(), retentionMonths, batchSize, enabled);
    }

    PracticeHistoryRetentionJob(
            PracticeHistoryRepository repository,
            Clock clock,
            int retentionMonths,
            int batchSize,
            boolean enabled
    ) {
        this.repository = repository;
        this.clock = clock;
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

    @Scheduled(
            fixedDelayString = "${PracticeHistory.RetentionIntervalSeconds:86400}",
            initialDelayString = "${PracticeHistory.RetentionInitialDelaySeconds:120}",
            timeUnit = TimeUnit.SECONDS
    )
    public void run() {
        if (retentionMonths < 1 || batchSize < 1) {
            logger.error(
                    "Practice history retention skipped: retentionMonths={} batchSize={}",
                    retentionMonths,
                    batchSize
            );
            return;
        }

        var cutoff = cutoff(clock.instant(), retentionMonths);
        var estimate = toEstimate(repository.summarizeCreatedBefore(cutoff));
        if (!enabled) {
            logger.info(
                    "Practice history retention dry-run: cutoff={} rows={} historyJsonBytes={}. PracticeHistory.RetentionEnabled=false",
                    cutoff,
                    estimate.rowCount(),
                    estimate.jsonBytes()
            );
            return;
        }

        var deleted = deleteOlderThan(cutoff);
        logger.info(
                "Practice history retention deleted {} rows older than {} (estimate before delete: rows={} historyJsonBytes={})",
                deleted,
                cutoff,
                estimate.rowCount(),
                estimate.jsonBytes()
        );
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

    static PracticeHistoryRetentionEstimate toEstimate(List<Object[]> rows) {
        if (rows == null || rows.isEmpty() || rows.get(0) == null || rows.get(0).length < 2) {
            return new PracticeHistoryRetentionEstimate(0, 0);
        }
        var row = rows.get(0);
        return new PracticeHistoryRetentionEstimate(toLong(row[0]), toLong(row[1]));
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    record PracticeHistoryRetentionEstimate(long rowCount, long jsonBytes) {}
}
