package com.esl.service

import com.esl.entity.dictation.Dictation
import com.esl.entity.rest.SearchDictationRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

import static com.esl.entity.dictation.Dictation.StudentLevel.JuniorPrimary
import static com.esl.entity.dictation.Dictation.StudentLevel.SeniorSecondary

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
        query                  | expectDictationIds
        "tam chi on"           | [4]
        "Tester"               | [1, 3]
        "tam.chi.on@gmail.com" | [4]
        "esl.com"              | [1, 3]
    }

    @Unroll
    def "Search dictation by keyword: query=#query"(String query, long[] expectDictationIds) {
        when: "search dictation"
        def request = new SearchDictationRequest().setKeyword(query)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        result.collect {it.id}.containsAll(expectDictationIds)

        where:
        query | expectDictationIds
        "tam dictation"      | [4]
        "Tam school"         | [5, 6]
        "Tam school exam"    | [5, 6]
        "P1 Tam school exam" | [5, 6]
        "Tam school test"    | [7, 8]
        "tam"                | [4, 5, 6, 7, 8]
    }

    @Unroll
    def "Search dictation by keyword='#keyword' and creator='#creator'"(String keyword, String creator, long[] expectDictationIds) {
        when: "search dictation"
        def request = new SearchDictationRequest().setKeyword(keyword).setCreator(creator)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        result.collect {it.id}.containsAll(expectDictationIds)

        where:
        keyword           | creator  | expectDictationIds
        "tam dictation 1" | "chi on" | [4]
    }

    @Unroll
    def "Search dictation by date"(Date min, Date max, boolean isResultContainDictation) {
        when: "search dictation"
        def request = new SearchDictationRequest().setMinDate(min).setMaxDate(max)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        !result.isEmpty() == isResultContainDictation

        where:
        min                    | max                    | isResultContainDictation
        null                   | null                   | true
        dateFrom('2018-01-01') | null                   | true
        dateFrom('2100-01-01') | null                   | false
        null                   | dateFrom('2100-01-01') | true
        null                   | dateFrom('1980-01-01') | false
        dateFrom('1980-01-01') | dateFrom('2100-01-01') | true
        dateFrom('1980-01-01') | dateFrom('1990-01-01') | false
        dateFrom('2101-01-01') | dateFrom('2102-01-01') | false
    }

    @Unroll
    def "Search dictation by suitable student: #studentLevel"(Dictation.StudentLevel studentLevel, long[] expectDictationIds) {
        when: "search dictation"
        def request = new SearchDictationRequest().setSuitableStudent(studentLevel)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        result.collect {it.id}.containsAll(expectDictationIds)

        where:
        studentLevel    | expectDictationIds
        JuniorPrimary   | [1, 2, 3, 5, 6, 7, 8]
        SeniorSecondary | [1, 2, 3, 4]
    }

    @Unroll
    def "Search dictation by id will return that dictation only"(String dictationId, boolean found) {
        when: "search dictation by Id"
        def request = new SearchDictationRequest().setKeyword(dictationId)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        if (found) {
            assert result.size() == 1
            assert result[0].id == dictationId.toLong()
        } else {
            assert result.size() == 0
        }

        where:
        dictationId | found
        "1" | true
        "3" | true
        "9999999" | false
    }

    def dateFrom(String date) {
        return new Date().parse("yyyy-MM-dd", date)
    }
}
