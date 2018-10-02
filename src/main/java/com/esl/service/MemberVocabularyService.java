package com.esl.service;

import com.esl.dao.repository.MemberVocabularyRepository;
import com.esl.entity.practice.MemberVocabulary;
import com.esl.entity.rest.VocabPracticeHistory;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberVocabularyService {
    private static Logger log = LoggerFactory.getLogger(MemberVocabularyService.class);

    @Autowired MemberVocabularyRepository memberVocabularyRepository;

    public MemberVocabulary updateResult(Member member, String word, boolean isCorrect) {
        var memberVocab = createOrGetMemberVocabulary(member, word);
        memberVocab.updateResult(isCorrect);
        memberVocabularyRepository.save(memberVocab);
        return memberVocab;
    }

    private MemberVocabulary createOrGetMemberVocabulary(Member member, String word) {
        var memberVocab = memberVocabularyRepository.findByIdMemberAndIdWord(member, word);
        return memberVocab.orElseGet(() -> saveNewMemberVocabulary(member, word));
    }

    private MemberVocabulary saveNewMemberVocabulary(Member member, String word) {
        var memberVocabulary = new MemberVocabulary(member, word);
        memberVocabularyRepository.save(memberVocabulary);
        return memberVocabulary;
    }

    public List<MemberVocabulary> saveHistory(Member member, List<VocabPracticeHistory> histories) {
        return histories.stream()
            .map(h -> updateResult(member, h.question.getWord(), h.correct))
            .collect(Collectors.toList());
    }
}
