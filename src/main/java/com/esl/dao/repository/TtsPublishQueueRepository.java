package com.esl.dao.repository;

import com.esl.entity.TtsPublishQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TtsPublishQueueRepository extends CrudRepository<TtsPublishQueue, Long> {
    @Query("select q from TtsPublishQueue q " +
            "where (q.nextAttemptAt is null or q.nextAttemptAt <= ?1) " +
            "and q.attemptCount < ?2 " +
            "order by q.lastUpdatedDate asc, q.createdDate asc")
    List<TtsPublishQueue> findNext(Date now, int maxAttempts, Pageable pageable);

    @Query("select count(q) from TtsPublishQueue q where q.attemptCount < ?1")
    long countActive(int maxAttempts);

    @Query("select count(q) from TtsPublishQueue q " +
            "where (q.nextAttemptAt is null or q.nextAttemptAt <= ?1) and q.attemptCount < ?2")
    long countReadyNow(Date now, int maxAttempts);

    long countByStatus(String status);

    @Query("select min(q.createdDate) from TtsPublishQueue q where q.attemptCount < ?1")
    Optional<Date> findOldestActiveCreatedDate(int maxAttempts);
}
