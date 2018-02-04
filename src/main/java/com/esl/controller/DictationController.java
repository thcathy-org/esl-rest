package com.esl.controller;

import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.model.dictation.DictationStatistics;
import com.esl.service.DictationService;
import com.esl.service.DictationStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.CacheManager;
import javax.cache.annotation.CacheResult;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/dictation")
public class DictationController {
    private static Logger log = LoggerFactory.getLogger(DictationController.class);

	@Value("${Dictation.MaxDictationStatistics}")
	public static int maxDictationStatistics = 5;

	@Autowired DictationStatService statService;
	@Autowired DictationDAO dictationDAO;
	@Autowired DictationService dictationService;

	@CacheResult(cacheName = "dictation")
    @RequestMapping(value = "/random-stat")
    public ResponseEntity<DictationStatistics> randomStatistics() {
		log.info("request random stat");

		try {
			return ResponseEntity.ok(statService.randomDictationStatistics(maxDictationStatistics));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(null);
		}
	}

	@RequestMapping(value = "/get/{id}")
	public ResponseEntity<Dictation> getDictationById(@PathVariable long id) {
		log.info("get dictation id: {}", id);

		try {
			return ResponseEntity.ok(dictationDAO.get(id));
		} catch (Exception e) {
			log.warn("fail in getting dictation", e);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}

	@RequestMapping(value = "/recommend/{id}")
	public ResponseEntity<Dictation> recommendDictation(@PathVariable long id) {
		log.info("get dictation id: {}", id);

		try {
			return ResponseEntity.ok(dictationService.recommendDictation(id));
		} catch (Exception e) {
			log.warn("fail in recommend dictation", e);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}

	@Component
	public static class CachingSetup implements JCacheManagerCustomizer
	{
		@Override
		public void customize(CacheManager cacheManager)
		{
			cacheManager.createCache("dictation", new MutableConfiguration<>()
					.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 1)))
					.setStoreByValue(false)
					.setStatisticsEnabled(true));
		}
	}
}