package com.esl.service

import com.esl.TestService
import com.esl.entity.rest.EditDictationRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class DictationServiceSpec extends Specification {
    @Autowired DictationService service
    @Autowired TestService testService

    def "create / amend dictation do not store duplicate vocabulary"() {
        when: "create dictation with duplicate vocabulary"
        def creationRequest = new EditDictationRequest()
        creationRequest.title = "Dictation service spec"
        creationRequest.vocabulary = [" apple ", "apple", " apple", "apple   ", "banana"]

        def dictation = service.createOrAmendDictation(testService.getTester1(), creationRequest)

        then: "dictation do not contain duplicate vocabulary"
        dictation.vocabs.size() == 2
        dictation.id > -1

        when: "update dictation with duplicate vocabulary"
        def amendRequest = new EditDictationRequest()
        amendRequest.dictationId = dictation.id
        amendRequest.title = dictation.title
        amendRequest.vocabulary = ["apple", " apple ", "cat"]
        def amendedDictation = service.createOrAmendDictation(testService.getTester1(), amendRequest)

        then: "dictation do not contain duplicate vocabulary"
        amendedDictation.vocabs.size() == 2
        amendedDictation.vocabs.any {it.word == "cat"}
        !amendedDictation.vocabs.any {it.word == "banana"}
        amendedDictation.id == dictation.id
    }
}
