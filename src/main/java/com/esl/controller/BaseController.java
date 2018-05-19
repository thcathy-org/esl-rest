package com.esl.controller;

import com.esl.dao.MemberDAO;
import com.esl.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public abstract class BaseController {

    @Autowired MemberDAO memberDAO;

    public Optional<Member> getSecurityContextMember() {
        return memberDAO.getMemberByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
