package com.esl.service


import com.esl.TestService
import com.esl.entity.dictation.Dictation
import com.esl.entity.rest.EditDictationRequest
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import static com.esl.entity.dictation.Dictation.Source.Select

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
        dictation.source == Dictation.Source.FillIn

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

        when: "cleanup dictation"
        service.deleteDictation(dictation.creator.emailAddress, dictation.id)

        then:
        1 == 1
    }

    def "change dictation from vocab to article and vice versa"() {
        when: "create dictation with duplicate vocabulary"
        def creationRequest = new EditDictationRequest()
        creationRequest.title = "Dictation service spec"
        creationRequest.vocabulary = ["apple"]
        def dictation = service.createOrAmendDictation(testService.getTester1(), creationRequest)

        then: "dictation do not contain duplicate vocabulary"
        dictation.vocabs.size() == 1
        StringUtils.isBlank(dictation.article)

        when: "change dictation from vocab to sentence"
        def amendRequest = new EditDictationRequest()
        amendRequest.dictationId = dictation.id
        amendRequest.vocabulary = []
        amendRequest.article = "sentence dictation"
        def amendedDictation = service.createOrAmendDictation(testService.getTester1(), amendRequest)

        then: "dictation's vocab are cleared"
        amendedDictation.vocabs.size() == 0
        amendedDictation.article == "sentence dictation"
        amendedDictation.id == dictation.id

        when: "change dictation from sentence to vocab"
        def amendRequest2 = new EditDictationRequest()
        amendRequest2.dictationId = dictation.id
        amendRequest2.vocabulary = ["banana"]
        amendRequest2.article = ""
        def amendedDictation2 = service.createOrAmendDictation(testService.getTester1(), amendRequest2)

        then: "dictation's article is cleared"
        amendedDictation2.vocabs.size() == 1
        StringUtils.isBlank(dictation.article)
        amendedDictation2.id == dictation.id

        when: "cleanup dictation"
        service.deleteDictation(dictation.creator.emailAddress, dictation.id)

        then:
        1 == 1
    }

    def "create selected vocabulary dictation"() {
        when: "create selected vocabulary dictation"
        def creationRequest = new EditDictationRequest()
        creationRequest.title = "Dictation service spec"
        creationRequest.vocabulary = ["apple"]
        creationRequest.source = Select
        def dictation = service.createOrAmendDictation(testService.getTester1(), creationRequest)

        then: "dictation source is Select"
        dictation.getSource() == Select

        when: "amend request send"
        def amendRequest = amendRequestFrom dictation
        amendRequest.source = null
        def amendedDictation = service.createOrAmendDictation(testService.getTester1(), amendRequest)

        then: "source never change"
        amendedDictation.source == Select
    }

    EditDictationRequest amendRequestFrom(Dictation from) {
        def request = new EditDictationRequest()
        request.title = from.title
        request.vocabulary = from.vocabs.collect { it -> it.word }
        request.dictationId = from.id
        return request
    }
}
