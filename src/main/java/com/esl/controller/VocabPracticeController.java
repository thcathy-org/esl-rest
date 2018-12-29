package com.esl.controller;

import com.esl.dao.MemberDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.enumeration.VocabDifficulty;
import com.esl.model.PhoneticQuestion;
import com.esl.service.VocabService;
import com.esl.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.annotation.CacheResult;

@RestController
@RequestMapping(value = "/vocab")
public class VocabPracticeController {
    private static Logger log = LoggerFactory.getLogger(VocabPracticeController.class);

    @Autowired
	VocabService vocabService;
    @Autowired
	MemberDAO memberDAO;

	@CacheResult(cacheName = "vocab")
    @RequestMapping(value = "/get/question/{word}")
    public ResponseEntity<PhoneticQuestion> getQuestion(
    		@PathVariable String word,
			@RequestParam(value="image", defaultValue = "1") boolean showImage) {
		try {
			if (ValidationUtil.isValidWord(word))
				return ResponseEntity.ok(vocabService.createQuestion(word, showImage));
			else
				return ResponseEntity.badRequest().body(null);
		} catch (Exception e) {
			log.error("Exception: ", e);
			return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(null);
		}
	}

	@RequestMapping(value = "/practice/generate/{studentLevel}")
	public ResponseEntity<Dictation> generatePractice(@PathVariable String studentLevel) {
		var difficulty = VocabDifficulty.valueOf(studentLevel);
		if (difficulty == null)
			return ResponseEntity.badRequest().body(null);

		return ResponseEntity.ok(vocabService.generatePractice(difficulty));
	}
}