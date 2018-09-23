package com.esl.controller

import com.esl.enumeration.VocabDifficulty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class VocabPracticeControllerSpec extends Specification {
    @Autowired private MockMvc mockMvc

    @Unroll
    def "generate practice in difficulty"(VocabDifficulty difficulty) {
        when:
        this.mockMvc.perform(
                get("/vocab/practice/generate/${difficulty.toString()}")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.vocabs', hasSize(3)))
                .andExpect(jsonPath('$.generated', is(true)))

        then:
        1 == 1

        where:
        difficulty << VocabDifficulty.values()
    }

    @Unroll
    def "Return bad request if invalid input: #input"(String input) {
        when:
        this.mockMvc.perform(
                get("/vocab/practice/generate/${input}")
        )
                .andExpect(status().is4xxClientError())

        then:
        1 == 1

        where:
        input << [null, '', 'invalid difficulty']
    }
}
