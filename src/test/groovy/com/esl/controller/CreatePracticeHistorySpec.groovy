package com.esl.controller

import com.esl.TestService
import com.esl.dao.repository.PracticeHistoryRepository
import com.esl.entity.rest.CreateDictationHistoryRequest
import com.esl.enumeration.ESLPracticeType
import com.esl.utils.MockMvcUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class CreatePracticeHistorySpec extends Specification {
    @Autowired private MockMvc mockMvc
    @Autowired ObjectMapper objectMapper
    @Autowired TestService testService
    @Autowired PracticeHistoryRepository practiceHistoryRepository

    @Unroll
    @IgnoreRest
    def "Create dictation history will create a new practice history"() {
        when:
        CreateDictationHistoryRequest request = new CreateDictationHistoryRequest()
        request.dictationId = 1
        request.mark = 1
        request.correct = 1
        request.wrong = 2
        request.histories = Collections.EMPTY_LIST
        request.historyJSON = "{json object}"
        this.mockMvc.perform(MockMvcUtils.postWithUserId("/dictation/history/create", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath('$.id', is(1)))
        def result = practiceHistoryRepository.findByMember(testService.tester1, Sort.by("createdDate")).get()

        then:
        result.size() >= 1
        result[0].historyJSON == "{json object}"
        result[0].correct == 1
        result[0].wrong == 2
        result[0].dictationId == 1
        result[0].eslPracticeType == ESLPracticeType.SentenceDictation
        result[0].createdDate.time <= new Date().time
    }

    @Unroll
    def "Always maintain 10 dictation history at max"() {
        when: "create 11 dictation history"
        CreateDictationHistoryRequest request = new CreateDictationHistoryRequest()
        request.dictationId = 1
        request.histories = Collections.EMPTY_LIST
        this.mockMvc.perform(MockMvcUtils.postWithUserId("/dictation/history/create", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath('$.id', is(1)))
        (2..11).each {
            request = new CreateDictationHistoryRequest()
            request.dictationId = 3
            request.histories = Collections.EMPTY_LIST
            this.mockMvc.perform(MockMvcUtils.postWithUserId("/dictation/history/create", objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath('$.id', is(3)))
        }
        def result = practiceHistoryRepository.findByMember(testService.tester1, Sort.by("createdDate")).get()

        then: "only 10 is storing last 10 history"
        result.size() == 10
        result.collect {it.dictationId}.contains(1) == false
    }

}
