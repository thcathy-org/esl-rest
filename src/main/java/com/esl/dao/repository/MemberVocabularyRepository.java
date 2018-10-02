package com.esl.dao.repository;


import com.esl.entity.practice.MemberScore;
import com.esl.entity.practice.MemberVocabulary;
import com.esl.model.Member;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public interface MemberVocabularyRepository extends PagingAndSortingRepository<MemberVocabulary, MemberVocabulary.MemberVocabularyId> {
    Optional<MemberVocabulary> findByIdMemberAndIdWord(Member member, String word);

    @Async
    CompletableFuture<List<MemberScore>> findByIdMember(Member member);
}