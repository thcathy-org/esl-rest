package com.esl.service;

import com.esl.dao.MemberDAO;
import com.esl.dao.dictation.DictationDAO;
import com.esl.dao.repository.MemberScoreRepository;
import com.esl.entity.rest.UpdateMemberRequest;
import com.esl.model.Member;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional
@Service
public class MemberService {
    private static Logger log = LoggerFactory.getLogger(MemberService.class);

    @Autowired
    private MemberScoreRepository memberScoreRepository;
    @Autowired private MemberVocabularyService memberVocabularyService;
    @Autowired private MemberScoreService memberScoreService;
    @Autowired private PracticeHistoryService practiceHistoryService;
    @Autowired private DictationDAO dictationDAO;
    @Autowired private MemberDAO memberDAO;

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

    public Member getOrCreate(Claims claims) {
        return memberDAO.getMemberByEmail(claims.get("email", String.class))
                .orElseGet(() -> createMember(claims));
    }

    public Member applyRequest(Member m, UpdateMemberRequest request) {
        m = memberDAO.merge(m);
        m.setName(request.lastName, request.firstName);
        m.setAddress(request.address);
        m.setPhoneNumber(request.phoneNumber);
        m.setSchool(request.school);
        m.setBirthday(request.birthday);
        m = memberDAO.persist(m);
        return m;
    }

    private Member createMember(Claims claims) {
        Member member = new Member();
        member.setEmailAddress(claims.get("email", String.class));
        member.setUserId(claims.getSubject());
        member.setCreatedDate(new Date());
        member.setName(claims.get("family_name", String.class), claims.get("given_name", String.class));
        memberDAO.persist(member);
        return member;
    }

}
