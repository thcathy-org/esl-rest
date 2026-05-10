package com.esl.dao.repository;

import com.esl.entity.VocabInterpretation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VocabInterpretationRepository extends CrudRepository<VocabInterpretation, Long> {
    Optional<VocabInterpretation> findByTextAndLang(String text, String lang);
}
