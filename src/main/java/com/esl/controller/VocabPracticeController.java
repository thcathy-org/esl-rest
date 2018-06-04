package com.esl.controller;

import java.util.concurrent.TimeUnit;

import javax.cache.CacheManager;
import javax.cache.annotation.CacheResult;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esl.model.PhoneticQuestion;
import com.esl.service.VocabService;
import com.esl.util.ValidationUtil;

@RestController
@RequestMapping(value = "/vocab")
public class VocabPracticeController {
    private static Logger log = LoggerFactory.getLogger(VocabPracticeController.class);

    @Autowired
	VocabService vocabService;

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
			return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(null);
		}
	}

	@Component
	public static class CachingSetup implements JCacheManagerCustomizer
	{
		@Override
		public void customize(CacheManager cacheManager)
		{
			cacheManager.createCache("vocab", new MutableConfiguration<>()
					.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 1)))
					.setStoreByValue(false)
					.setStatisticsEnabled(true));
		}
	}
}