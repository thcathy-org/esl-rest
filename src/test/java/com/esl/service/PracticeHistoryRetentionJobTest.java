package com.esl.service;

import com.esl.dao.repository.PracticeHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracticeHistoryRetentionJobTest {

    private static final Instant NOW = Instant.parse("2026-10-28T11:46:00Z");

    @Mock PracticeHistoryRepository repository;

    @Test
    void cutoff_subtractsCalendarMonthsInUtc() {
        var cutoff = PracticeHistoryRetentionJob.cutoff(NOW, 12);

        assertEquals(Date.from(Instant.parse("2025-10-28T11:46:00Z")), cutoff);
    }

    @Test
    void cutoff_clampsEndOfMonth() {
        var cutoff = PracticeHistoryRetentionJob.cutoff(Instant.parse("2025-03-31T00:30:00Z"), 1);

        assertEquals(Date.from(Instant.parse("2025-02-28T00:30:00Z")), cutoff);
    }

    @Test
    void cutoff_clampsLeapDay() {
        var cutoff = PracticeHistoryRetentionJob.cutoff(Instant.parse("2024-02-29T16:00:00Z"), 12);

        assertEquals(Date.from(Instant.parse("2023-02-28T16:00:00Z")), cutoff);
    }

    @Test
    void cutoff_rejectsNonPositiveMonths() {
        assertThrows(IllegalArgumentException.class, () -> PracticeHistoryRetentionJob.cutoff(NOW, 0));
    }

    @Test
    void run_whenDisabled_doesNotQuery() {
        var job = job(false, 12, 500);

        job.run();

        verifyNoInteractions(repository);
    }

    @Test
    void run_whenEnabled_deletesFullBatchThenPartialBatch() {
        var page = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));
        when(repository.findIdsByCreatedDateBefore(any(), eq(page)))
                .thenReturn(List.of(1L, 2L))
                .thenReturn(List.of(3L));
        when(repository.deleteByIdIn(any())).thenAnswer(invocation -> ((List<?>) invocation.getArgument(0)).size());
        var job = job(true, 12, 2);

        job.run();

        verify(repository, times(2)).findIdsByCreatedDateBefore(any(), eq(page));
        verify(repository).deleteByIdIn(List.of(1L, 2L));
        verify(repository).deleteByIdIn(List.of(3L));
        verify(repository, times(2)).deleteByIdIn(any());
    }

    @Test
    void run_whenEnabled_stopsAfterEmptyPage() {
        var page = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));
        when(repository.findIdsByCreatedDateBefore(any(), eq(page)))
                .thenReturn(List.of(8L, 9L))
                .thenReturn(List.of());
        when(repository.deleteByIdIn(List.of(8L, 9L))).thenReturn(2);
        var job = job(true, 12, 2);

        job.run();

        verify(repository, times(2)).findIdsByCreatedDateBefore(any(), eq(page));
        verify(repository, times(1)).deleteByIdIn(any());
    }

    @Test
    void run_whenEnabled_secondRunWithNoRowsDoesNotDelete() {
        when(repository.findIdsByCreatedDateBefore(any(), any())).thenReturn(List.of());
        var job = job(true, 12, 500);

        job.run();
        job.run();

        verify(repository, times(2)).findIdsByCreatedDateBefore(any(), any());
        verify(repository, never()).deleteByIdIn(any());
    }

    @Test
    void run_whenDeleteRemovesNothing_stopsInsteadOfLooping() {
        when(repository.findIdsByCreatedDateBefore(any(), any())).thenReturn(List.of(4L, 5L));
        when(repository.deleteByIdIn(any())).thenReturn(0);
        var job = job(true, 12, 2);

        job.run();

        verify(repository, times(1)).findIdsByCreatedDateBefore(any(), any());
        verify(repository, times(1)).deleteByIdIn(List.of(4L, 5L));
    }

    @Test
    void run_skipsQueriesWhenRetentionMonthsAreNotPositive() {
        var job = job(true, 0, 500);

        job.run();

        verifyNoInteractions(repository);
    }

    @Test
    void run_skipsQueriesWhenBatchSizeIsNotPositive() {
        var job = job(true, 12, 0);

        job.run();

        verifyNoInteractions(repository);
    }

    private PracticeHistoryRetentionJob job(boolean enabled, int months, int batchSize) {
        return new PracticeHistoryRetentionJob(repository, months, batchSize, enabled);
    }
}
