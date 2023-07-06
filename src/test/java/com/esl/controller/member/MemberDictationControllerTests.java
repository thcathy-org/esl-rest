package com.esl.controller.member;

import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.Vocab;
import com.esl.entity.rest.EditDictationRequest;
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

import java.util.Arrays;

import static com.esl.entity.dictation.Dictation.Source.FillIn;
import static com.esl.security.JWTAuthorizationFilter.TESTING_HEADER;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
		EditDictationRequest request = createNewDictationRequest(true);
		request.title = "shouldFail";

		this.mockMvc.perform(post("/member/dictation/edit"))
				.andExpect(status().isUnauthorized());

		assertThat(dictationDAO.listNewCreated(1).get(0).getTitle(), not("shouldFail"));
	}

	@Test
	public void createNewWordDictation() throws Exception {
		EditDictationRequest request = createNewDictationRequest(true);
		request.wordContainSpace = true;

		this.mockMvc.perform(MockMvcUtils.postWithUserId("/member/dictation/edit", objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.title", is("new dictation")))
				.andExpect(jsonPath("$.createdDate").exists())
				.andExpect(jsonPath("$.wordContainSpace", is(true)))
				.andExpect(jsonPath("$.source", is(FillIn.name())))
				.andExpect(jsonPath("$.includeAIImage", is(true)))
				.andExpect(jsonPath("$.id", greaterThan(0)));

		Dictation dictation = dictationDAO.listNewCreated(1).get(0);
		assertThat(dictation.getTitle(), is("new dictation"));
		assertThat(dictation.getVocabs().isEmpty(), is(false));
		assertThat(dictation.getArticle(), isEmptyOrNullString());
		assertThat(dictation.isWordContainSpace(), is(true));
		assertThat(dictation.getSource(), is(FillIn));
		assertThat(dictation.isIncludeAIImage(), is(true));
	}

	@Test
	public void createNewSentenceDictation() throws Exception {
		EditDictationRequest request = createNewDictationRequest(false);

		this.mockMvc.perform(MockMvcUtils.postWithUserId("/member/dictation/edit", objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.title", is("new dictation")))
				.andExpect(jsonPath("$.article", is("It is a sentence dictation")))
				.andExpect(jsonPath("$.createdDate").exists())
				.andExpect(jsonPath("$.sentenceLength", is("Long")))
				.andExpect(jsonPath("$.source", is(FillIn.name())))
				.andExpect(jsonPath("$.id", greaterThan(0)));

		Dictation dictation = dictationDAO.listNewCreated(1).get(0);
		assertThat(dictation.getTitle(), is("new dictation"));
		assertThat(dictation.getVocabs().isEmpty(), is(true));
		assertThat(dictation.getArticle(), is("It is a sentence dictation"));
		assertThat(dictation.getSource(), is(FillIn));
	}

	@Test
	public void editWordDictation() throws Exception {
		EditDictationRequest request = new EditDictationRequest();
		request.dictationId = 1;
		request.title = "Testing v2";
		request.vocabulary = Arrays.asList("apple", "bus", "car");
		request.showImage = false;
		request.sentenceLength = "Normal";
		request.source = FillIn;

		this.mockMvc.perform(MockMvcUtils.postWithUserId("/member/dictation/edit", objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("Testing v2")))
				.andExpect(jsonPath("$.lastModifyDate").exists())
				.andExpect(jsonPath("$.source", is(FillIn.name())))
				.andExpect(jsonPath("$.id", greaterThan(0)));

		Dictation d = dictationDAO.get(1L);
		assertThat(d.getTitle(), is("Testing v2"));
		assertThat(d.isShowImage(), is(false));
		assertThat(d.getVocabs().stream().map(Vocab::getWord).collect(toList()),
				containsInAnyOrder("car","bus","apple"));
		assertThat(d.getVocabs().stream().filter(v -> v.getWord().equals("apple")).findFirst().get().getTotalCorrect(), is(2));
		assertThat(d.getSource(), is(FillIn));
	}

	@Test
	public void editSentenceDictation() throws Exception {
		EditDictationRequest request = new EditDictationRequest();
		request.dictationId = 3;
		request.title = "Sentence Dictation 1";
		request.article = "Updated article";
		request.includeAIImage = true;

		this.mockMvc.perform(MockMvcUtils.postWithUserId("/member/dictation/edit", objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("Sentence Dictation 1")))
				.andExpect(jsonPath("$.article", is("Updated article")))
				.andExpect(jsonPath("$.lastModifyDate").exists())
				.andExpect(jsonPath("$.includeAIImage", is(true)))
				.andExpect(jsonPath("$.id", greaterThan(0)));

		Dictation d = dictationDAO.get(3L);
		assertThat(d.getArticle(), is("Updated article"));
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
				.header(TESTING_HEADER, "tester2@esl.com"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void deleteDictationWithCorrectUser_shouldSuccess() throws Exception {
		this.mockMvc.perform(
				get("/member/dictation/delete/2").header(TESTING_HEADER, "tester@esl.com")
		).andExpect(status().isOk());

		assertThat(dictationDAO.get(2L), nullValue());
	}

	@Test
	public void deleteDictationWithWrongUser_shouldFail() throws Exception {
		this.mockMvc.perform(
				get("/member/dictation/delete/1").header(TESTING_HEADER, "tester2@esl.com")
		).andExpect(status().is5xxServerError());

		assertThat(dictationDAO.get(1L), notNullValue());
	}

	@Test
	public void deleteNotExistsDictation_shouldFail() throws Exception {
		this.mockMvc.perform(get("/member/dictation/delete/99999999").header(TESTING_HEADER, "tester2@esl.com"))
				.andExpect(status().is5xxServerError());
	}

	private EditDictationRequest createNewDictationRequest(boolean isWord) {
		EditDictationRequest request = new EditDictationRequest();
		request.title = "new dictation";
		request.suitableStudent = Dictation.StudentLevel.JuniorPrimary;
		request.includeAIImage = true;
		if (isWord) {
			request.vocabulary = Arrays.asList("apple", "bus", "car");
		} else {
			request.article = "It is a sentence dictation";
			request.sentenceLength = "Long";
		}

		return request;
	}

}
