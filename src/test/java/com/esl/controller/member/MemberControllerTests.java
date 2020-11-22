package com.esl.controller.member;

import com.esl.dao.MemberDAO;
import com.esl.entity.rest.UpdateMemberRequest;
import com.esl.model.Member;
import com.esl.service.JWTService;
import com.esl.utils.MockMvcUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class MemberControllerTests {

	@Autowired private MockMvc mockMvc;
	@Autowired private MemberDAO memberDAO;
	@Autowired ObjectMapper objectMapper;

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

	@Test
	public void updateNotExistMember_shouldReturnFail() throws Exception {
		this.mockMvc.perform(MockMvcUtils.postWithEmail("/member/profile/update", objectMapper.writeValueAsString(new UpdateMemberRequest()), "not@exist.com"))
				.andExpect(status().is5xxServerError());
	}

	@Test
	public void updateMember_shouldStoreUpdateInDB() throws Exception {
		var request = new UpdateMemberRequest();
		request.address = "testing address";
		request.firstName = "first name";
		request.lastName = "last name";
		request.phoneNumber = "43420024";
		request.school = "a school";
		request.birthday = new Date(1528100000000L);
		this.mockMvc.perform(MockMvcUtils.postWithUserId("/member/profile/update", objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.address", is(request.address)))
				.andExpect(jsonPath("$.name.firstName", is(request.firstName)))
				.andExpect(jsonPath("$.name.lastName", is(request.lastName)))
				.andExpect(jsonPath("$.phoneNumber", is(request.phoneNumber)))
				.andExpect(jsonPath("$.birthday", is("2018-06-04T08:13:20.000+00:00")))
				.andExpect(jsonPath("$.school", is(request.school)));

		var m = memberDAO.getMemberByEmail("tester@esl.com").get();
		assertThat(m.getAddress(), is(request.address));
		assertThat(m.getName().getFullName(), is( request.firstName+ ' ' + request.lastName));
		assertThat(m.getPhoneNumber(), is(request.phoneNumber));
		assertThat(m.getSchool(), is(request.school));
		assertThat(m.getBirthday().getTime(), is(1528100000000L));
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
