package com.esl.controller.member;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esl.controller.MemberAware;
import com.esl.dao.MemberDAO;
import com.esl.dao.repository.MemberScoreRepository;
import com.esl.entity.practice.MemberScore;

@RestController
@RequestMapping(value = "/member/ranking")
public class MemberRankingController implements MemberAware {
	private static Logger log = LoggerFactory.getLogger(MemberDictationController.class);

	@Autowired MemberDAO memberDAO;
	@Autowired MemberScoreRepository memberScoreRepository;

	@Override public MemberDAO getMemberDAO() { return memberDAO; }

	@RequestMapping(value = "/score/alltimes-and-last6")
	public ResponseEntity<List<MemberScore>> allTimesAndLast6MemberScore() {
		log.info("get last 6 member score");

		try {
			return ResponseEntity.ok(
					memberScoreRepository.findByMemberAndScoreYearMonthGreaterThanEqual(getSecurityContextMember().get(), MemberScore.lastSixMonth())
							.exceptionally(throwable -> new ArrayList<>())
							.join()
			);
		} catch (Exception e) {
			log.warn("fail when get last 6 member score", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}
}