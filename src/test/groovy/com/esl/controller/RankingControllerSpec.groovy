package com.esl.controller

import com.esl.TestService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.hasSize
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class RankingControllerSpec extends Specification {
    @Autowired private MockMvc mockMvc
    @Autowired ObjectMapper objectMapper
    @Autowired TestService testService

    @Unroll
    def "start with normal article will return practice page: #memberEmail"(String memberEmail, int expectTotalScore) {
        when:
        this.mockMvc.perform(
                get("/member/ranking/score/alltimes-and-last6").header("email", memberEmail)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(expectTotalScore)))

        then:
        1 == 1

        where:
        memberEmail       | expectTotalScore
        "tester@esl.com"  | 7
        "tester2@esl.com" | 0
    }

}
