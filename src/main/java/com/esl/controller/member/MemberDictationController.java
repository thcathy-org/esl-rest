package com.esl.controller.member;

import com.esl.dao.MemberDAO;
import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.rest.CreateDictationRequest;
import com.esl.model.Member;
import com.esl.service.DictationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/member/dictation")
public class MemberDictationController {
    private static Logger log = LoggerFactory.getLogger(MemberDictationController.class);

	@Autowired DictationDAO dictationDAO;
	@Autowired DictationService dictationService;
	@Autowired MemberDAO memberDAO;

    @RequestMapping(value = "/create")
    public ResponseEntity<Dictation> createDictation(@RequestBody CreateDictationRequest request) {
		log.info("create a new dictation");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			Member member = memberDAO.getMemberByUserID(authentication.getName());
			log.info("created by userId: {}", member.getUserId());

			return ResponseEntity.ok(dictationService.createAndSaveDictation(member,request));
		} catch (Exception e) {
			log.warn("fail in create dictation", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	@RequestMapping(value = "/getall")
	public ResponseEntity<List<Dictation>> getMyDictations() {
    	log.info("get all dictations");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			log.info("userId: {}", authentication.getName());
			Member member = memberDAO.getMemberByUserID(authentication.getName());

			return ResponseEntity.ok(dictationDAO.listByMember(member));
		} catch (Exception e) {
			log.warn("fail in get all dictations", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

	}

}