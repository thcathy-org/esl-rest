package com.esl;

import com.esl.dao.MemberDAO;
import com.esl.dao.PhoneticQuestionDAO;
import com.esl.model.Member;
import com.esl.model.PhoneticQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @Autowired MemberDAO memberDAO;
    @Autowired PhoneticQuestionDAO phoneticQuestionDAO;

    public Member getTester1() {
        return memberDAO.get(1l);
    }

    public PhoneticQuestion getPhoneticQuestionAeroplane() {
        return phoneticQuestionDAO.getPhoneticQuestionByWord("aeroplane");
    }
}
