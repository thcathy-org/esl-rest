package com.esl.controller;

import com.esl.entity.practice.MemberScoreRanking;
import com.esl.service.RankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.CacheManager;
import javax.cache.annotation.CacheResult;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/ranking")
public class RankingController {
    private static Logger log = LoggerFactory.getLogger(RankingController.class);

    @Autowired
	RankingService rankingService;

	@CacheResult(cacheName = "ranking")
    @RequestMapping(value = "/random-top-score")
    public ResponseEntity<MemberScoreRanking> randomTopScore() {
		log.debug("request random top score");

		try {
			return ResponseEntity.ok(rankingService.randomTopScore().get(10, TimeUnit.SECONDS));
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
			cacheManager.createCache("ranking", new MutableConfiguration<>()
					.setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 1)))
					.setStoreByValue(false)
					.setStatisticsEnabled(true));
		}
	}
}