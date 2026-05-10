package com.esl.controller;

import com.esl.service.InterpretationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class InterpretationControllerTests {

    @Autowired private MockMvc mockMvc;
    @MockBean private InterpretationService interpretationService;

    @Test
    public void interpret_happyPath_returnsPlainTextWithCacheControl() throws Exception {
        when(interpretationService.interpret("elephant", "zh-Hant")).thenReturn("大象");

        this.mockMvc.perform(get("/interpretation").param("text", "elephant").param("lang", "zh-Hant"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string("大象"))
                .andExpect(header().string("Cache-Control", containsString("max-age=86400")))
                .andExpect(header().string("Cache-Control", containsString("public")));
    }

    @Test
    public void interpret_invalidLang_returnsBadRequest() throws Exception {
        when(interpretationService.interpret(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Unsupported lang: fr"));

        this.mockMvc.perform(get("/interpretation").param("text", "hello").param("lang", "fr"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unsupported lang")));
    }

    @Test
    public void interpret_blankText_returnsEmptyOk() throws Exception {
        when(interpretationService.interpret("   ", "en")).thenReturn("");

        this.mockMvc.perform(get("/interpretation").param("text", "   ").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void interpret_serviceException_returns500() throws Exception {
        when(interpretationService.interpret(anyString(), anyString()))
                .thenThrow(new RuntimeException("LLM upstream failure"));

        this.mockMvc.perform(get("/interpretation").param("text", "hello").param("lang", "en"))
                .andExpect(status().isInternalServerError());
    }

}
