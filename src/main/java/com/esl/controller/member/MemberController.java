package com.esl.controller.member;

import io.jsonwebtoken.Claims;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esl.dao.MemberDAO;
import com.esl.model.Member;
import com.esl.service.JWTService;

@RestController
@RequestMapping(value = "/member/profile")
public class MemberController {
    private static Logger log = LoggerFactory.getLogger(MemberController.class);

	@Autowired MemberDAO memberDAO;
	@Autowired JWTService jwtService;

	@RequestMapping(value = "/get")
	public ResponseEntity<Member> getOrCreate(HttpServletRequest request) {
		try {
			String token = request.getHeader("Authorization");
			Claims claims = jwtService.parseClaims(token).get();

			Member member =
					memberDAO.getMemberByEmail(claims.get("email", String.class))
					.orElseGet(() -> createMember(claims));

			log.info("Get member: {}", member);
			return ResponseEntity.ok(member);
		} catch (Exception e) {
			log.warn("fail to get or create member", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	private Member createMember(Claims claims) {
		Member member = new Member();
		member.setEmailAddress(claims.get("email", String.class));
		member.setUserId(claims.getSubject());
		member.setCreatedDate(new Date());
		member.setName(claims.get("family_name", String.class), claims.get("given_name", String.class));
		memberDAO.persist(member);
		return member;
	}

}