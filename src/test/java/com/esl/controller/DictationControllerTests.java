package com.esl.controller;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.esl.entity.rest.CreateDictationHistoryRequest;
import com.esl.entity.rest.VocabPracticeHistory;
import com.esl.utils.MockMvcUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class DictationControllerTests {

	@Autowired private MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@Test
	public void getADictation() throws Exception {
		this.mockMvc.perform(get("/dictation/get/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.title", containsString("Testing")));
	}

	@Test
	public void addScoreToMemberWhenCreateHistory() throws Exception {
		CreateDictationHistoryRequest request = new CreateDictationHistoryRequest();
		request.dictationId = 1;
		request.mark = 1;
		request.histories = Arrays.asList(
			new VocabPracticeHistory().setAnswer("").setCorrect(true)
		);
		this.mockMvc.perform(MockMvcUtils.postWithUserId("/dictation/history", objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.title", is("new dictation")))
				.andExpect(jsonPath("$.createdDate").exists())
				.andExpect(jsonPath("$.id", greaterThan(0)));

		this.mockMvc.perform(get("/dictation/history"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.title", containsString("Testing")));
	}
}
