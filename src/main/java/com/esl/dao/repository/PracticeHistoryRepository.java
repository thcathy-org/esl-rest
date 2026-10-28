package com.esl.dao.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.esl.entity.practice.PracticeHistory;
import com.esl.model.Member;

@Repository
public interface PracticeHistoryRepository extends CrudRepository<PracticeHistory, Long> {
    @Async
    CompletableFuture<List<PracticeHistory>> findByMember(Member member, Sort sort);

    void deleteByMember(Member member);

    long countByCreatedDateBefore(Date cutoff);

    @Query("select h.id from PracticeHistory h where h.createdDate < ?1")
    List<Long> findIdsByCreatedDateBefore(Date cutoff, Pageable pageable);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PracticeHistory h where h.id in ?1")
    int deleteByIdIn(Collection<Long> ids);
}
