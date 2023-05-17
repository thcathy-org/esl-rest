package com.esl.controller.member;

import com.esl.controller.MemberAware;
import com.esl.dao.MemberDAO;
import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.rest.UpdateMemberRequest;
import com.esl.model.Member;
import com.esl.service.JWTService;
import com.esl.service.MemberScoreService;
import com.esl.service.MemberVocabularyService;
import com.esl.service.PracticeHistoryService;
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
	@Autowired DictationDAO dictationDAO;
	@Autowired
	MemberScoreService memberScoreService;
	@Autowired
	PracticeHistoryService practiceHistoryService;
	@Autowired
	MemberVocabularyService memberVocabularyService;

	@Override
	public MemberDAO getMemberDAO() { return memberDAO; }

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

	@PostMapping(value = "/update")
	public ResponseEntity<Member> update(@RequestBody UpdateMemberRequest request) {
		Member member = getSecurityContextMember()
				.map(m -> applyRequest(m, request))
				.map(memberDAO::persist)
				.get();
		return ResponseEntity.ok(member);
	}

	@DeleteMapping(value = "/delete")
	public HttpStatus delete() {
		return getSecurityContextMember()
				.map(this::deleteMember)
				.orElse(Boolean.FALSE) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
	}

	private Boolean deleteMember(Member m) {
		// be-careful to the sequence
		memberVocabularyService.deleteByMember(m);
		memberScoreService.deleteByMember(m);
		practiceHistoryService.deleteByMember(m);
		dictationDAO.listByMember(m).forEach(d -> dictationDAO.remove(d));
		memberDAO.delete(m);
		return true;
	}

	private Member applyRequest(Member m, UpdateMemberRequest request) {
		m = memberDAO.merge(m);
		m.setName(request.lastName, request.firstName);
		m.setAddress(request.address);
		m.setPhoneNumber(request.phoneNumber);
		m.setSchool(request.school);
		m.setBirthday(request.birthday);
		return m;
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
