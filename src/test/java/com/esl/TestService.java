package com.esl;

import com.esl.dao.MemberDAO;
import com.esl.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @Autowired MemberDAO memberDAO;

    public Member getTester1() {
        return memberDAO.get(1l);
    }
}
