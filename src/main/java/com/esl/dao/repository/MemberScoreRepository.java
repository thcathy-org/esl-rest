package com.esl.dao.repository;


import com.esl.entity.practice.MemberScore;
import com.esl.model.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public interface MemberScoreRepository extends PagingAndSortingRepository<MemberScore, String> {
    Optional<MemberScore> findByMemberAndScoreYearMonth(Member member, int scoreYearMonth);

    @Async
    CompletableFuture<List<MemberScore>> findByMemberAndScoreYearMonthGreaterThanEqual(Member member, int greaterEqualScoreYearMonth);

    @Async
    @Query("select s from MemberScore s where s.scoreYearMonth = ?1 and (s.score > ?2 or (s.score = ?2 and s.lastUpdatedDate < ?3)) order by score, lastUpdatedDate desc")
    CompletableFuture<List<MemberScore>> findTop5HigherScore(int scoreYearMonth, int minScore, Date lastUpdatedDate);

    @Async
    @Query("select s from MemberScore s where s.scoreYearMonth = ?1 and (s.score < ?2 or (s.score = ?2 and s.lastUpdatedDate > ?3)) order by score, lastUpdatedDate desc")
    CompletableFuture<List<MemberScore>> findTop5LowerScore(int scoreYearMonth, int minScore, Date lastUpdatedDate);

    @Async
    @Query("select count(s.id) from MemberScore s where s.scoreYearMonth = ?1 and (s.score > ?2 or (s.score = ?2 and s.lastUpdatedDate < ?3))")
    CompletableFuture<Long> countHigherScore(int scoreYearMonth, int score, Date lastUpdatedDate);

    @Async
    @Query("select s from MemberScore s where s.scoreYearMonth = ?1 and s.score > 0 order by s.score desc, s.lastUpdatedDate")
    CompletableFuture<List<MemberScore>> findTopScore(int scoreYearMonth, Pageable pageable);

    void deleteByMember(Member member);
}
