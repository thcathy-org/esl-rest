package com.esl.controller

import com.esl.TestService
import com.esl.dao.repository.PracticeHistoryRepository
import com.esl.entity.rest.CreateDictationHistoryRequest
import com.esl.utils.MockMvcUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
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
    def "Create dictation history will create a new practice history"() {
        when:
        CreateDictationHistoryRequest request = new CreateDictationHistoryRequest()
        request.dictationId = 1
        request.mark = 1
        request.histories = Collections.EMPTY_LIST
        request.historyJSON = "{json object}"
        this.mockMvc.perform(MockMvcUtils.postWithUserId("/dictation/history/create", objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath('$.id', is(1)))
        def result = practiceHistoryRepository.findByMember(testService.tester1, new Sort("createdDate")).get()

        then:
        result.size() >= 1
        result[0].historyJSON = "{json object}"
    }

}
