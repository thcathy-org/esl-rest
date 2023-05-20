package com.esl.service;

import com.esl.dao.MemberDAO;
import com.esl.dao.dictation.DictationDAO;
import com.esl.dao.repository.MemberScoreRepository;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Transactional
@Service
public class MemberService {
    private static Logger log = LoggerFactory.getLogger(MemberService.class);

    @Resource private MemberScoreRepository memberScoreRepository;
    @Resource private MemberVocabularyService memberVocabularyService;
    @Resource private MemberScoreService memberScoreService;
    @Resource private PracticeHistoryService practiceHistoryService;
    @Resource private DictationDAO dictationDAO;
    @Resource private MemberDAO memberDAO;

    public Boolean deleteMember(Member m) {
        log.info("Delete member {}:{}", m.getId(), m.getEmailAddress());
        // be-careful to the sequence
        m = (Member) memberDAO.attachSession(m);
        memberVocabularyService.deleteByMember(m);
        memberScoreService.deleteByMember(m);
        practiceHistoryService.deleteByMember(m);
        dictationDAO.listByMember(m).forEach(d -> dictationDAO.remove(d));
        memberDAO.delete(m);
        return true;
    }

}
