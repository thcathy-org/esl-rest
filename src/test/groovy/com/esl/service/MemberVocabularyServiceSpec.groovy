package com.esl.service

import com.esl.TestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class MemberVocabularyServiceSpec extends Specification {
    @Autowired MemberVocabularyService memberVocabularyService
    @Autowired TestService testService

    @Unroll
    def "update member vocabulary"(String word, boolean isCorrect, int expectedCorrect, int expectedWrong) {
        when: "update a member vocabulary"
        def memberVocabulary = memberVocabularyService.updateResult(testService.getTester1(), word, isCorrect)

        then: "the value are updated"
        memberVocabulary.correct == expectedCorrect
        memberVocabulary.wrong == expectedWrong

        where:
        word         | isCorrect | expectedCorrect | expectedWrong
        "test-apple" | true      | 4               | 0
        "banana"     | false     | 0               | 1
    }

}
