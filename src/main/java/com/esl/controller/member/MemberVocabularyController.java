package com.esl.controller.member;

import com.esl.controller.MemberAware;
import com.esl.dao.MemberDAO;
import com.esl.entity.practice.MemberVocabulary;
import com.esl.entity.rest.VocabPracticeHistory;
import com.esl.service.MemberVocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/member/vocab")
public class MemberVocabularyController implements MemberAware {
    private static Logger log = LoggerFactory.getLogger(MemberVocabularyController.class);

    @Autowired MemberVocabularyService memberVocabularyService;
    @Autowired MemberDAO memberDAO;

	@Override public MemberDAO getMemberDAO() { return memberDAO; }

	@PostMapping(value = "/practice/history/save")
	public ResponseEntity<List<MemberVocabulary>> saveHistory(@RequestBody List<VocabPracticeHistory> histories) {
		return ResponseEntity.ok(memberVocabularyService.saveHistory(getSecurityContextMember().get(),histories));
	}
}