package com.esl.controller;

import com.esl.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ArticleDictationControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void divideArticle() throws Exception {
		String article = String.join("\n",
				"Lorem ipsum dolor sit amet,",
				"consectetur adipiscing elit,",
				"sed do eiusmod tempor incididunt",
				"ut labore et dolore magna aliqua."
		);

		this.mockMvc.perform(
					post("/dictation/article/divide")
							.content(TestUtil.convertObjectToJsonBytes(article))
		)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$", hasSize(4)));
	}
}
