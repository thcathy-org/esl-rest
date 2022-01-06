package com.esl.service

import com.esl.TestService
import com.esl.dao.dictation.DictationDAO
import com.esl.dao.repository.MemberVocabularyRepository
import com.esl.entity.practice.MemberScore
import com.esl.entity.practice.MemberVocabulary
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
    @Autowired MemberScoreService memberScoreService
    @Autowired DictationDAO dictationDAO
    @Autowired MemberVocabularyRepository memberVocabularyRepository

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
    def "saveHistory"(boolean includeDictationId) {
        given:
        def member = testService.getTester1()
        def request = new SaveMemberVocabularyHistoryRequest()
        def vocabHistory1 = new VocabPracticeHistory()
        vocabHistory1.question = testService.getPhoneticQuestionAeroplane();
        vocabHistory1.correct = true
        def thisMonthScore = memberScoreService.findOrCreateMemberScore(member, MemberScore.thisMonth())
        def allTimesScore = memberScoreService.findOrCreateMemberScore(member, MemberScore.allTimesMonth())
        def dictation = testService.newPersistedSelectVocabDictation();
        if (includeDictationId) request.dictationId = dictation.id
        request.histories = [vocabHistory1] as List<VocabPracticeHistory>
        def memberVocab = memberVocabularyRepository.findByIdMemberAndIdWord(member, vocabHistory1.question.word)

        when: "save history"
        def result = memberVocabularyService.saveHistory(member, request)

        then: "verify result"
        result.size() == 1
        result.get(0).correct == memberVocab.orElse(new MemberVocabulary()).correct + 1
        result.get(0).id.word == vocabHistory1.question.word
        memberScoreService.findOrCreateMemberScore(member, MemberScore.thisMonth()).score == thisMonthScore.score + 1
        memberScoreService.findOrCreateMemberScore(member, MemberScore.allTimesMonth()).score == allTimesScore.score + 1
        if (includeDictationId) {
            assert dictationDAO.get(dictation.id).totalAttempt == dictation.totalAttempt + 1
        }

        where:
        includeDictationId << [true, false]
    }

}
