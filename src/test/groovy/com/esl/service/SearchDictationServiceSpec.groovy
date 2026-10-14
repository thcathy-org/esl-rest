package com.esl.service

import com.esl.TestService
import com.esl.dao.dictation.DictationDAO
import com.esl.entity.dictation.Dictation
import com.esl.entity.dictation.DictationSearchSort
import com.esl.entity.rest.SearchDictationRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

import java.text.SimpleDateFormat

import static com.esl.entity.dictation.Dictation.StudentLevel.Any
import static com.esl.entity.dictation.Dictation.StudentLevel.JuniorPrimary
import static com.esl.entity.dictation.Dictation.StudentLevel.SeniorSecondary

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SearchDictationServiceSpec extends Specification {
    @Autowired SearchDictationService service
    @Autowired DictationService dictationService
    @Autowired DictationDAO dictationDAO
    @Autowired TestService testService
    @Autowired ObjectMapper objectMapper

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
    def "Search dictation by suitable student: #studentLevel"(Dictation.StudentLevel studentLevel, long[] expectDictationIds, int expectSize) {
        when: "search dictation"
        def request = new SearchDictationRequest().setSuitableStudent(studentLevel)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        result.size() == expectSize
        result.collect {it.id}.containsAll(expectDictationIds)

        where:
        studentLevel    | expectDictationIds        | expectSize
        JuniorPrimary   | [1, 2, 3, 5, 6, 7, 8]    | 7
        SeniorSecondary | [1, 2, 3, 4]             | 4
        Any             | [1, 2, 3, 4, 5, 6, 7, 8] | 8
        null            | [1, 2, 3, 4, 5, 6, 7, 8] | 8
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

    @Unroll
    def "Search dictation by type: #type"(String type, long[] expectDictationIds) {
        when: "search dictation"
        def request = new SearchDictationRequest().setType(type)
        def result = service.searchDictation(request, Integer.MAX_VALUE)

        then:
        result.size() == expectDictationIds.size()
        result.collect {it.id}.containsAll(expectDictationIds)

        where:
        type      | expectDictationIds
        ""        | [1, 2, 3, 4, 5, 6, 7, 8]
        null      | [1, 2, 3, 4, 5, 6, 7, 8]
        "Vocab"   | [1, 2]
        "Article" | [3, 4, 5, 6, 7, 8]
    }

    def "null sort orders by createdDate"() {
        given:
        def createdEarlier = saveSortRow("sort-created-earlier", dateFrom("2010-01-01"), dateFrom("2020-01-01"), 50, 50)
        def createdLater = saveSortRow("sort-created-later", dateFrom("2020-01-01"), dateFrom("2010-01-01"), 1, 1)

        when:
        def result = service.searchDictation(new SearchDictationRequest().setKeyword("sort-created").setSearchTitle(false), 50)

        then:
        result*.id == [createdLater.id, createdEarlier.id]
        result.every { it.description && it.creator && it.vocabs != null }

        cleanup:
        deleteDictation(createdLater?.id)
        deleteDictation(createdEarlier?.id)
    }

    def "mostPracticed orders by totalAttempt"() {
        given:
        def fewer = saveSortRow("sort-attempt-fewer", dateFrom("2020-01-01"), dateFrom("2020-01-01"), 1, 9)
        def more = saveSortRow("sort-attempt-more", dateFrom("2010-01-01"), dateFrom("2010-01-01"), 9, 1)

        when:
        def result = service.searchDictation(new SearchDictationRequest().setKeyword("sort-attempt").setSearchTitle(false).setSort(DictationSearchSort.mostPracticed), 50)

        then:
        result*.id == [more.id, fewer.id]

        cleanup:
        deleteDictation(fewer?.id)
        deleteDictation(more?.id)
    }

    def "mostRecommended orders by totalRecommended"() {
        given:
        def fewer = saveSortRow("sort-recommend-fewer", dateFrom("2020-01-01"), dateFrom("2020-01-01"), 9, 1)
        def more = saveSortRow("sort-recommend-more", dateFrom("2010-01-01"), dateFrom("2010-01-01"), 1, 9)

        when:
        def result = service.searchDictation(new SearchDictationRequest().setKeyword("sort-recommend").setSearchTitle(false).setSort(DictationSearchSort.mostRecommended), 50)

        then:
        result*.id == [more.id, fewer.id]

        cleanup:
        deleteDictation(fewer?.id)
        deleteDictation(more?.id)
    }

    def "lastModified orders by lastModifyDate"() {
        given:
        def editedEarlier = saveSortRow("sort-edited-earlier", dateFrom("2020-01-01"), dateFrom("2010-01-01"), 9, 9)
        def editedLater = saveSortRow("sort-edited-later", dateFrom("2010-01-01"), dateFrom("2020-01-01"), 1, 1)

        when:
        def result = service.searchDictation(new SearchDictationRequest().setKeyword("sort-edited").setSearchTitle(false).setSort(DictationSearchSort.lastModified), 50)

        then:
        result*.id == [editedLater.id, editedEarlier.id]

        cleanup:
        deleteDictation(editedEarlier?.id)
        deleteDictation(editedLater?.id)
    }

    def "keyword plus mostPracticed filters then orders by totalAttempt"() {
        given:
        def fewer = saveSortRow("sort-filter-fewer", dateFrom("2020-01-01"), dateFrom("2020-01-01"), 2, 0)
        def more = saveSortRow("sort-filter-more", dateFrom("2010-01-01"), dateFrom("2010-01-01"), 8, 0)
        def other = saveSortRow("unrelated-attempt-row", dateFrom("2021-01-01"), dateFrom("2021-01-01"), 100, 0)

        when:
        def result = service.searchDictation(new SearchDictationRequest().setKeyword("sort-filter").setSearchTitle(false).setSort(DictationSearchSort.mostPracticed), 50)

        then:
        result*.id == [more.id, fewer.id]

        cleanup:
        deleteDictation(fewer?.id)
        deleteDictation(more?.id)
        deleteDictation(other?.id)
    }

    def "numeric keyword ignores sort"() {
        when:
        def result = service.searchDictation(new SearchDictationRequest().setKeyword("1").setSort(DictationSearchSort.mostPracticed), 50)

        then:
        result.size() == 1
        result[0].id == 1L
    }

    def "unknown and blank sort json become newest"() {
        expect:
        objectMapper.readValue('{"sort":"nope"}', SearchDictationRequest).sort == DictationSearchSort.newest
        objectMapper.readValue('{"sort":""}', SearchDictationRequest).sort == DictationSearchSort.newest
        objectMapper.readValue('{"sort":"lastModified"}', SearchDictationRequest).sort == DictationSearchSort.lastModified
        objectMapper.readValue('{}', SearchDictationRequest).sort == null
    }

    private Dictation saveSortRow(String description, Date createdDate, Date lastModifyDate, int totalAttempt, int totalRecommended) {
        def dictation = new Dictation("sort " + description)
        dictation.description = description
        dictation.creator = testService.tester1
        dictation.source = Dictation.Source.FillIn
        dictation.suitableStudent = Any
        dictation.createdDate = createdDate
        dictation.lastModifyDate = lastModifyDate
        dictation.totalAttempt = totalAttempt
        dictation.totalRecommended = totalRecommended
        dictationDAO.persist(dictation)
        dictationDAO.flush()
        return dictation
    }

    private void deleteDictation(Long id) {
        if (id == null) return
        try {
            dictationService.deleteDictation(testService.tester1.emailAddress, id)
        } catch (UnsupportedOperationException ignored) {
        }
    }

    def dateFrom(String date) {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
}
