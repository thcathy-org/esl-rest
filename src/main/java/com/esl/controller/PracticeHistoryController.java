package com.esl.controller;

import com.esl.dao.MemberDAO;
import com.esl.dao.repository.PracticeHistoryRepository;
import com.esl.entity.practice.PracticeHistory;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/practice-history")
public class PracticeHistoryController implements MemberAware {
    private static Logger log = LoggerFactory.getLogger(PracticeHistoryController.class);

    @Autowired PracticeHistoryRepository practiceHistoryRepository;
	@Autowired MemberDAO memberDAO;

	@Override public MemberDAO getMemberDAO() { return memberDAO; }

    @RequestMapping(value = "/get-all")
    public List<PracticeHistory> getAll() {
		Member member = getSecurityContextMember().get();
		return practiceHistoryRepository.findByMember(member, Sort.by(Sort.Direction.DESC, "createdDate")).join();
	}
}
