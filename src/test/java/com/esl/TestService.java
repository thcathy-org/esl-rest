package com.esl;

import com.esl.dao.MemberDAO;
import com.esl.dao.PhoneticQuestionDAO;
import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.model.Member;
import com.esl.model.PhoneticQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TestService {
    @Autowired MemberDAO memberDAO;
    @Autowired PhoneticQuestionDAO phoneticQuestionDAO;
    @Autowired DictationDAO dictationDAO;

    public Member getTester1() {
        return memberDAO.get(1l);
    }

    public PhoneticQuestion getPhoneticQuestionAeroplane() {
        return phoneticQuestionDAO.getPhoneticQuestionByWord("aeroplane");
    }

    public Dictation newPersistedSelectVocabDictation() {
        var dictation = new Dictation(UUID.randomUUID().toString());
        dictation.setSource(Dictation.Source.Select);
        return dictationDAO.persist(dictation);
    }
}
