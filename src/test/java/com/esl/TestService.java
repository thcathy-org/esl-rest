package com.esl;

import com.esl.dao.MemberDAO;
import com.esl.dao.PhoneticQuestionDAO;
import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.Vocab;
import com.esl.model.Member;
import com.esl.model.Name;
import com.esl.model.PhoneticQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class TestService {
    @Autowired MemberDAO memberDAO;
    @Autowired PhoneticQuestionDAO phoneticQuestionDAO;
    @Autowired DictationDAO dictationDAO;

    public Member getTester1() {
        return memberDAO.get(1l);
    }

    @Transactional
    public Member withMember(String memberEmail) {
        var member = new Member(memberEmail, new Name("", ""), null, "", "", "", null, false, memberEmail);
        return memberDAO.persist(member);
    }

    @Transactional
    public Dictation withDictation(Member member, Dictation.Source source) {
        var dictation = new Dictation(UUID.randomUUID().toString());
        dictation.setSource(Dictation.Source.Select);
        dictation.setCreator(member);

        var word1 = new Vocab("word1");
        word1.setDictation(dictation);
        var word2 = new Vocab("word2");
        word2.setDictation(dictation);
        dictation.setVocabs(List.of(word1, word2));

        return dictationDAO.persist(dictation);
    }

    public PhoneticQuestion getPhoneticQuestionAeroplane() {
        return phoneticQuestionDAO.getPhoneticQuestionByWord("aeroplane");
    }
}
