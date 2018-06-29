package com.esl.controller;

import java.util.concurrent.TimeUnit;

import javax.cache.annotation.CacheResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

}