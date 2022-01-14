package com.esl.controller.member;

import com.esl.controller.MemberAware;
import com.esl.dao.MemberDAO;
import com.esl.dao.repository.MemberVocabularyRepository;
import com.esl.entity.practice.MemberVocabulary;
import com.esl.entity.rest.SaveMemberVocabularyHistoryRequest;
import com.esl.entity.rest.VocabPracticeHistory;
import com.esl.service.MemberVocabularyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/member/vocab")
public class MemberVocabularyController implements MemberAware {
    private static Logger log = LoggerFactory.getLogger(MemberVocabularyController.class);

    @Autowired MemberVocabularyService memberVocabularyService;
    @Autowired MemberDAO memberDAO;
	@Autowired MemberVocabularyRepository memberVocabularyRepository;

	@Override public MemberDAO getMemberDAO() { return memberDAO; }

	@PostMapping(value = "/practice/history/save/v2")
	public ResponseEntity<List<MemberVocabulary>> saveHistoryV2(@RequestBody SaveMemberVocabularyHistoryRequest request) {
		return ResponseEntity.ok(memberVocabularyService.saveHistory(getSecurityContextMember().get(), request));
	}

	@PostMapping(value = "/practice/history/save")
	public ResponseEntity<List<MemberVocabulary>> saveHistory(@RequestBody List<VocabPracticeHistory> histories) {
		var request = new SaveMemberVocabularyHistoryRequest();
		request.histories = histories;
		request.dictationId = -1;
		return saveHistoryV2(request);
	}

	@GetMapping(value = "/practice/history/getall")
	public ResponseEntity<List<MemberVocabulary>> getAll() {
		return ResponseEntity.ok(memberVocabularyRepository.findByIdMember(getSecurityContextMember().get()).join());
	}
}
