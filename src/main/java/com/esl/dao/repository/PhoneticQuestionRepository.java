package com.esl.dao.repository;


import com.esl.model.PhoneticQuestion;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneticQuestionRepository extends PagingAndSortingRepository<PhoneticQuestion, Long> {

}