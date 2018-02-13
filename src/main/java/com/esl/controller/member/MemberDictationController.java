package com.esl.controller.member;

import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.rest.CreateDictationHistoryRequest;
import com.esl.model.dictation.DictationStatistics;
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

@RestController
@RequestMapping(value = "/member/dictation")
public class MemberDictationController {
    private static Logger log = LoggerFactory.getLogger(MemberDictationController.class);

	@Autowired DictationDAO dictationDAO;
	@Autowired DictationService dictationService;

    @RequestMapping(value = "/create")
    public ResponseEntity<DictationStatistics> createDictation(@RequestBody CreateDictationHistoryRequest request) {
		log.info("create a new dictation");

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			log.info("authentication.getName(): {}", authentication.getName());

			return ResponseEntity.ok(null);
		} catch (Exception e) {
			log.warn("fail in create dictation", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

}