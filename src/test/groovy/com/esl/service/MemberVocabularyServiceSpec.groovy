package com.esl.service

import com.esl.TestService
import com.esl.entity.rest.SaveMemberVocabularyHistoryRequest
import com.esl.entity.rest.VocabPracticeHistory
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

    @Unroll
    def "saveHistory"() {
        when: "save history"
        def request = new SaveMemberVocabularyHistoryRequest()
        def vocabHistory1 = new VocabPracticeHistory()
        vocabHistory1.question = testService.getPhoneticQuestionAeroplane();
        vocabHistory1.correct = true
        request.histories = [vocabHistory1] as List<VocabPracticeHistory>
        def result = memberVocabularyService.saveHistory(testService.getTester1(), request)

        then: "verify result"
        result.size() == 1
        result.get(0).correct == 1
        result.get(0).id.word == vocabHistory1.question.word
    }

}
