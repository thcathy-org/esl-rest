package com.esl.service

import com.esl.entity.rest.SearchDictationRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SearchDictationServiceSpec extends Specification {
    @Autowired SearchDictationService service

    @Unroll
    def "Search dictation by creator: query=#query"(String query, long[] expectDictationIds) {
        when: "search dictation"
        def request = new SearchDictationRequest().setCreator(query)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        result.collect {it.id}.containsAll(expectDictationIds)

        where:
        query | expectDictationIds
        "tam chi on" | [4]
    }

}
