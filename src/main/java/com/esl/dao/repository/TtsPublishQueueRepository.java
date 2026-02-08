package com.esl.dao.repository;

import com.esl.entity.TtsPublishQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TtsPublishQueueRepository extends CrudRepository<TtsPublishQueue, Long> {
    @Query("select q from TtsPublishQueue q " +
            "where q.status in ?1 and (q.nextAttemptAt is null or q.nextAttemptAt <= ?2) " +
            "order by q.lastUpdatedDate asc, q.createdDate asc")
    List<TtsPublishQueue> findNext(List<String> statuses, Date now, Pageable pageable);
}
