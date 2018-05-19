package com.esl.controller;

import com.esl.dao.MemberDAO;
import com.esl.model.Member;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public interface MemberAware {

    MemberDAO getMemberDAO();

    default Optional<Member> getSecurityContextMember() {
        return getMemberDAO().getMemberByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    }

}
