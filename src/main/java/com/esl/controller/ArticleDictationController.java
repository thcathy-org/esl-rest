package com.esl.controller;

import com.esl.service.ArticleDictationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/dictation/article")
public class ArticleDictationController {
    private static Logger log = LoggerFactory.getLogger(ArticleDictationController.class);

    @Autowired ArticleDictationService articleDictationService;

	@PostMapping(value = "/divide")
	public ResponseEntity<List<String>> divideArticle(@RequestBody String article) {
		log.info("divide article to sentences: {}", article);

		try {
			return ResponseEntity.ok(articleDictationService.divideArticleToSentences(article));
		} catch (Exception e) {
			log.warn("fail in divide article", e);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
}