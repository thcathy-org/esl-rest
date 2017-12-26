package com.esl.dao.repository;


import com.esl.entity.practice.QuestionHistory;
import com.esl.enumeration.ESLPracticeType;
import com.esl.model.Member;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionHistoryRepository extends PagingAndSortingRepository<QuestionHistory, String> {
    List<QuestionHistory> findByMemberAndPracticeTypeAndWord(Member member, ESLPracticeType practiceType, String word);
    List<QuestionHistory> findByMember(Member member);
}