package com.esl.service;

import com.esl.dao.dictation.IDictationDAO;
import com.esl.dao.repository.MemberVocabularyRepository;
import com.esl.entity.practice.MemberVocabulary;
import com.esl.entity.rest.SaveMemberVocabularyHistoryRequest;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberVocabularyService {
    private static Logger log = LoggerFactory.getLogger(MemberVocabularyService.class);

    @Autowired MemberVocabularyRepository memberVocabularyRepository;
    @Autowired IDictationDAO dictationDAO;
    @Autowired MemberScoreService memberScoreService;

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

    public MemberVocabulary saveNewMemberVocabulary(Member member, String word) {
        var memberVocabulary = new MemberVocabulary(member, word);
        memberVocabularyRepository.save(memberVocabulary);
        return memberVocabulary;
    }

    @Transactional
    public List<MemberVocabulary> saveHistory(Member member, SaveMemberVocabularyHistoryRequest request) {
        var dictation = Optional.ofNullable(dictationDAO.get(request.dictationId));
        dictation.ifPresent(d -> {
            d = dictationDAO.merge(d);
            d.setLastPracticeDate(new Date());
            d.setTotalAttempt(d.getTotalAttempt()+1);
            dictationDAO.persist(d);
        });

        memberScoreService.addScoreToMember(member, request.totalCorrect());

        return request.histories.stream()
            .map(h -> updateResult(member, h.question.getWord(), h.correct))
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteByMember(Member member) {
        log.info("delete all MemberVocabulary by member {}:{}", member.getId(), member.getEmailAddress());
        memberVocabularyRepository.deleteByIdMember(member);
    }
}
