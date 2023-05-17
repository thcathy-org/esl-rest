package com.esl.dao.repository;


import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import com.esl.entity.practice.PracticeHistory;
import com.esl.model.Member;

@Repository
public interface PracticeHistoryRepository extends PagingAndSortingRepository<PracticeHistory, Long> {
    @Async
    CompletableFuture<List<PracticeHistory>> findByMember(Member member, Sort sort);

    void deleteByMember(Member member);
}
