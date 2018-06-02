package com.esl.controller;

import com.esl.TestService;
import com.esl.dao.repository.MemberScoreRepository;
import com.esl.entity.practice.MemberScore;
import com.esl.entity.practice.MemberScoreRanking;
import com.esl.entity.rest.CreateDictationHistoryRequest;
import com.esl.service.RankingService;
import com.esl.utils.MockMvcUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
		this.mockMvc.perform(get("/dictation/get/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.title", containsString("Testing")));
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
				.andExpect(content().contentType("application/json;charset=UTF-8"))
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
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.id", is(1)));
	}
}
