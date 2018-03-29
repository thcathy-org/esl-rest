package com.esl.controller.member;

import com.esl.dao.MemberDAO;
import com.esl.model.Member;
import com.esl.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class MemberControllerTests {

	@Autowired private MockMvc mockMvc;
	@Autowired private MemberDAO memberDAO;

	@MockBean
	private JWTService jwtService;

	@Test
	public void getOrCreate_givenNewEmail_shouldStoreNewMemberObject() throws Exception {
		given(jwtService.parseClaims(any())).willReturn(newUserClaims());

		this.mockMvc.perform(
				get("/member/profile/get")
						.header("email", "newtester@a.com")
		)
			.andExpect(status().isOk());

		Member member = memberDAO.getMemberByEmail("newtester@a.com").get();
		assertThat(member.getName().getFirstName(), is("new"));
		assertThat(member.getName().getLastName(), is("tester"));
	}

	private Optional<Claims> newUserClaims() {
		HashMap<String, Object> values = new HashMap<>();
		values.put("email", "newtester@a.com");
		values.put("given_name", "new");
		values.put("family_name", "tester");
		values.put("sub", UUID.randomUUID().toString());

		return Optional.of(new DefaultClaims(values));
	}
}
