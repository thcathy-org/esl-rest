package com.esl.controller;

import com.esl.TestService;
import com.esl.dao.repository.MemberScoreRepository;
import com.esl.entity.practice.MemberScore;
import com.esl.entity.rest.CreateDictationHistoryRequest;
import com.esl.utils.MockMvcUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class DictationControllerTests {

	@Autowired private MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired MemberScoreRepository memberScoreRepository;
	@Autowired TestService testService;

	@Test
	public void getADictation() throws Exception {
		var result = this.mockMvc.perform(get("/dictation/get/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.title", containsString("Testing")))
				.andExpect(jsonPath("$.sentenceLength", containsString("Normal")))
				.andExpect(jsonPath("$.wordContainSpace", Matchers.is(false)))
				.andReturn();
		System.out.println(result.getResponse().getContentAsString());
	}

	@Test
	public void getNotExistDictation() throws Exception {
		this.mockMvc.perform(get("/dictation/get/9999999"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("Dictation not found")));
	}

	@Test
	public void addScoreToMemberWhenCreateHistory() throws Exception {
		int orgScore = memberScoreRepository.findByMemberAndScoreYearMonth(testService.getTester1(), MemberScore.thisMonth()).get().getScore();
		CreateDictationHistoryRequest request = new CreateDictationHistoryRequest();
		request.dictationId = 1;
		request.mark = 1;
		request.histories = Collections.EMPTY_LIST;

		this.mockMvc.perform(MockMvcUtils.postWithUserId("/dictation/history/create", objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.id", is(1)));

		MemberScore s = memberScoreRepository.findByMemberAndScoreYearMonth(testService.getTester1(), MemberScore.thisMonth()).get();
		assertThat(s.getScore(), is(orgScore + 1));
	}

	@Test
	public void createHistoryWithoutMemberShouldSuccess() throws Exception {
		CreateDictationHistoryRequest request = new CreateDictationHistoryRequest();
		request.dictationId = 1;
		request.mark = 1;
		request.histories = Collections.EMPTY_LIST;

		this.mockMvc.perform(
				post("/dictation/history/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
		)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.id", is(1)));
	}
}
