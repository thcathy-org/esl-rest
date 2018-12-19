package com.esl.controller


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
class MemberVocabularyControllerSpec extends Specification {
    @Autowired private MockMvc mockMvc

    @Unroll
    def "get all vocabulary"(String email, int expectedSize) {
        when:
        this.mockMvc.perform(
                get("/member/vocab/practice/history/getall").header("email", email)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(expectedSize)))

        then:
        1 == 1

        where:
        email             | expectedSize
        "hoi.nam@esl.com"  | 1
        "tester2@esl.com" | 0

    }
}
