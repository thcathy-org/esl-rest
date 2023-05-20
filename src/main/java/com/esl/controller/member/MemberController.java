package com.esl.controller.member;

import com.esl.controller.MemberAware;
import com.esl.dao.MemberDAO;
import com.esl.entity.rest.UpdateMemberRequest;
import com.esl.model.Member;
import com.esl.service.JWTService;
import com.esl.service.MemberService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping(value = "/member/profile")
public class MemberController implements MemberAware {
    private static Logger log = LoggerFactory.getLogger(MemberController.class);

	@Autowired JWTService jwtService;
	@Autowired MemberDAO memberDAO;
	@Autowired
	MemberService memberService;

	@Override
	public MemberDAO getMemberDAO() { return memberDAO; }

	@RequestMapping(value = "/get")
	public ResponseEntity<Member> getOrCreate(HttpServletRequest request) {
		try {
			String token = request.getHeader("Authorization");
			Claims claims = jwtService.parseClaims(token).orElseThrow();

			Member member = memberService.getOrCreate(claims);
			log.info("Get member: {}", member);
			return ResponseEntity.ok(member);
		} catch (Exception e) {
			log.warn("fail to get or create member", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	@PostMapping(value = "/update")
	public ResponseEntity<Member> update(@RequestBody UpdateMemberRequest request) {
		Member member = getSecurityContextMember()
				.map(m -> memberService.applyRequest(m, request))
				.orElseThrow();
		return ResponseEntity.ok(member);
	}

	@DeleteMapping(value = "/delete")
	public HttpStatus delete() {
		return getSecurityContextMember()
				.map(memberService::deleteMember)
				.orElse(Boolean.FALSE) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
	}

}
