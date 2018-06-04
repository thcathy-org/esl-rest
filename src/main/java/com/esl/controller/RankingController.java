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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esl.entity.practice.MemberScoreRanking;
import com.esl.service.RankingService;

@RestController
@RequestMapping(value = "/ranking")
public class RankingController {
    private static Logger log = LoggerFactory.getLogger(RankingController.class);

    @Autowired
	RankingService rankingService;

	@CacheResult(cacheName = "ranking")
    @RequestMapping(value = "/random-top-score")
    public ResponseEntity<MemberScoreRanking> randomTopScore() throws Exception {
		return ResponseEntity.ok(rankingService.randomTopScore().get(10, TimeUnit.SECONDS));
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