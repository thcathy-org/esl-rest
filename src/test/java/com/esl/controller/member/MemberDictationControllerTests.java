package com.esl.controller.member;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.Vocab;
import com.esl.entity.rest.EditDictationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class MemberDictationControllerTests {

	@Autowired private MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired DictationDAO dictationDAO;

	@Test
	public void postEditWithoutAuthentication_shouldFail() throws Exception {
		EditDictationRequest request = createNewDictationRequest();
		request.title = "shouldFail";

		this.mockMvc.perform(post("/member/dictation/edit"))
			.andExpect(status().isUnauthorized());

		assertThat(dictationDAO.listNewCreated(1).get(0).getTitle(), not("shouldFail"));
	}

	@Test
	public void createNewDictation() throws Exception {
		EditDictationRequest request = createNewDictationRequest();

		this.mockMvc.perform(postWithUserId("/member/dictation/edit", request))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.title", is("new dictation")))
				.andExpect(jsonPath("$.createdDate").exists())
				.andExpect(jsonPath("$.id", greaterThan(0)));

		assertThat(dictationDAO.listNewCreated(1).get(0).getTitle(), is("new dictation"));
	}

	@Test
	public void editDictation() throws Exception {
		EditDictationRequest request = new EditDictationRequest();
		request.dictationId = 1;
		request.title = "Testing v2";
		request.vocabulary = Arrays.asList("apple", "bus", "car");

		this.mockMvc.perform(postWithUserId("/member/dictation/edit", request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("Testing v2")))
				.andExpect(jsonPath("$.lastModifyDate").exists())
				.andExpect(jsonPath("$.id", greaterThan(0)));

		Dictation d = dictationDAO.get(1L);
		assertThat(d.getTitle(), is("Testing v2"));
		assertThat(d.getVocabs().stream().map(Vocab::getWord).collect(toList()),
				containsInAnyOrder("car","bus","apple"));
		assertThat(d.getVocabs().stream().filter(v -> v.getWord().equals("apple")).findFirst().get().getTotalCorrect(), is(2));
	}

	@Test
	public void editDictationWithWrongUser_shouldFail() throws Exception {
		EditDictationRequest request = new EditDictationRequest();
		request.dictationId = 1;
		request.title = "Testing v2";
		request.vocabulary = Arrays.asList("apple", "bus", "car");

		this.mockMvc.perform( post("/member/dictation/edit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.header("UserId", "tester2"))
				.andExpect(status().isBadRequest());
		assertThat(dictationDAO.get(1L).getTitle(), is("Testing 1"));
	}

	private EditDictationRequest createNewDictationRequest() {
		EditDictationRequest request = new EditDictationRequest();
		request.title = "new dictation";
		request.suitableStudent = Dictation.StudentLevel.JuniorPrimary;
		request.vocabulary = Arrays.asList("apple", "bus", "car");
		return request;
	}

	private MockHttpServletRequestBuilder postWithUserId(String path, Object request) throws JsonProcessingException {
		return post(path)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.header("UserId", "tester");
	}
}
