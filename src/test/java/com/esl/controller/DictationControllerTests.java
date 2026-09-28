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
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
	public void searchWithoutShortPayloadReturnsFullDictation() throws Exception {
		postSearch("{\"keyword\":\"1\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$[0].title", is("Testing 1")))
				.andExpect(jsonPath("$[0].vocabs", hasSize(2)))
				.andExpect(jsonPath("$[0].vocabs[*].word", containsInAnyOrder("apple", "zoo")))
				.andExpect(jsonPath("$[0].creator.emailAddress", is("tester@esl.com")));

		postSearch("{\"keyword\":\"1\",\"shortPayload\":false}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].vocabs", hasSize(2)))
				.andExpect(jsonPath("$[0].creator.emailAddress", is("tester@esl.com")));
	}

	@Test
	public void searchShortPayloadOmitsVocabsArticleAndCreator() throws Exception {
		var body = postSearch("{\"keyword\":\"1\",\"shortPayload\":true}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$[0].title", is("Testing 1")))
				.andExpect(jsonPath("$[0].suitableStudent", is("Any")))
				.andExpect(jsonPath("$[0].totalAttempt", greaterThanOrEqualTo(0)))
				.andExpect(jsonPath("$[0].totalRecommended", greaterThanOrEqualTo(0)))
				.andExpect(jsonPath("$[0].source", is("FillIn")))
				.andExpect(jsonPath("$[0].questionCount", is(2)))
				.andExpect(jsonPath("$[0].type", is("Vocab")))
				.andExpect(jsonPath("$[0].sentenceDictation").doesNotExist())
				.andExpect(jsonPath("$[0].createdDate").exists())
				.andExpect(jsonPath("$[0].vocabs").doesNotExist())
				.andExpect(jsonPath("$[0].article").doesNotExist())
				.andExpect(jsonPath("$[0].creator").doesNotExist())
				.andReturn().getResponse().getContentAsString();

		assertThat(body, not(containsString("apple")));
		assertThat(body, not(containsString("tester@esl.com")));
	}

	@Test
	public void searchShortPayloadSentenceDictationHasNoArticleText() throws Exception {
		var body = postSearch("{\"keyword\":\"3\",\"shortPayload\":true}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(3)))
				.andExpect(jsonPath("$[0].type", is("Article")))
				.andExpect(jsonPath("$[0].sentenceDictation").doesNotExist())
				.andExpect(jsonPath("$[0].questionCount", is(1)))
				.andExpect(jsonPath("$[0].article").doesNotExist())
				.andExpect(jsonPath("$[0].vocabs").doesNotExist())
				.andExpect(jsonPath("$[0].creator").doesNotExist())
				.andReturn().getResponse().getContentAsString();

		assertThat(body, not(containsString("It is a sentence dictation")));
	}

	private ResultActions postSearch(String json) throws Exception {
		return this.mockMvc.perform(post("/dictation/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json));
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
