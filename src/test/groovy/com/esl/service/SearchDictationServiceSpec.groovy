package com.esl.service

import com.esl.TestService
import com.esl.dao.dictation.DictationDAO
import com.esl.entity.dictation.Dictation
import com.esl.entity.dictation.DictationSearchSort
import com.esl.entity.rest.SearchDictationRequest
import com.esl.service.tts.DictationSentenceChunker
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.hibernate.SessionFactory
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
    @Autowired EntityManager entityManager
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

    def "default search stays full and shortPayload matches filters order and cap"() {
        when:
        def keyword = new SearchDictationRequest().setKeyword("tam")
        def full = service.searchDictation(keyword, 50)
        def summaries = service.searchDictationSummary(new SearchDictationRequest().setKeyword("tam").setShortPayload(true), 50)

        then:
        full.size() > 1
        full.every { it instanceof Dictation && it.article }
        (summaries*.id as Set) == (full*.id as Set)
        summaries.every { it.type == Dictation.DictationType.Article && it.questionCount == 1 }

        when: "same type filter"
        def vocabFull = service.searchDictation(new SearchDictationRequest().setType("Vocab"), Integer.MAX_VALUE)
        def vocabShort = service.searchDictationSummary(new SearchDictationRequest().setType("Vocab").setShortPayload(true), Integer.MAX_VALUE)

        then:
        (vocabShort*.id as Set) == (vocabFull*.id as Set)
        vocabShort.every { it.type == Dictation.DictationType.Vocab }
        vocabShort.find { it.id == 1 }.questionCount == 2
        vocabShort.find { it.id == 2 }.questionCount == 2

        when: "same cap and the same rows when every hit is returned"
        def cappedFull = service.searchDictation(new SearchDictationRequest(), 3)
        def cappedShort = service.searchDictationSummary(new SearchDictationRequest().setShortPayload(true), 3)
        def allFull = service.searchDictation(new SearchDictationRequest(), Integer.MAX_VALUE)
        def allShort = service.searchDictationSummary(new SearchDictationRequest().setShortPayload(true), Integer.MAX_VALUE)

        then:
        cappedFull.size() == 3
        cappedShort.size() == 3
        (allShort*.id as Set) == (allFull*.id as Set)
    }

    def "null sort orders full and short search by createdDate"() {
        given:
        def createdEarlier = saveSortRow("sort-created-earlier", dateFrom("2010-01-01"), dateFrom("2020-01-01"), 50, 50)
        def createdLater = saveSortRow("sort-created-later", dateFrom("2020-01-01"), dateFrom("2010-01-01"), 1, 1)

        when:
        def ids = searchIds(new SearchDictationRequest().setKeyword("sort-created").setSearchTitle(false))

        then:
        ids == [createdLater.id, createdEarlier.id]

        cleanup:
        deleteDictation(createdLater?.id)
        deleteDictation(createdEarlier?.id)
    }

    def "mostPracticed orders by totalAttempt"() {
        given:
        def fewer = saveSortRow("sort-attempt-fewer", dateFrom("2020-01-01"), dateFrom("2020-01-01"), 1, 9)
        def more = saveSortRow("sort-attempt-more", dateFrom("2010-01-01"), dateFrom("2010-01-01"), 9, 1)

        when:
        def ids = searchIds(new SearchDictationRequest().setKeyword("sort-attempt").setSearchTitle(false).setSort(DictationSearchSort.mostPracticed))

        then:
        ids == [more.id, fewer.id]

        cleanup:
        deleteDictation(fewer?.id)
        deleteDictation(more?.id)
    }

    def "mostRecommended orders by totalRecommended"() {
        given:
        def fewer = saveSortRow("sort-recommend-fewer", dateFrom("2020-01-01"), dateFrom("2020-01-01"), 9, 1)
        def more = saveSortRow("sort-recommend-more", dateFrom("2010-01-01"), dateFrom("2010-01-01"), 1, 9)

        when:
        def ids = searchIds(new SearchDictationRequest().setKeyword("sort-recommend").setSearchTitle(false).setSort(DictationSearchSort.mostRecommended))

        then:
        ids == [more.id, fewer.id]

        cleanup:
        deleteDictation(fewer?.id)
        deleteDictation(more?.id)
    }

    def "lastModified orders by lastModifyDate"() {
        given:
        def editedEarlier = saveSortRow("sort-edited-earlier", dateFrom("2020-01-01"), dateFrom("2010-01-01"), 9, 9)
        def editedLater = saveSortRow("sort-edited-later", dateFrom("2010-01-01"), dateFrom("2020-01-01"), 1, 1)

        when:
        def ids = searchIds(new SearchDictationRequest().setKeyword("sort-edited").setSearchTitle(false).setSort(DictationSearchSort.lastModified))

        then:
        ids == [editedLater.id, editedEarlier.id]

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
        def ids = searchIds(new SearchDictationRequest().setKeyword("sort-filter").setSearchTitle(false).setSort(DictationSearchSort.mostPracticed))

        then:
        ids == [more.id, fewer.id]

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

    def "numeric keyword short search returns one summary and skips other filters"() {
        when:
        def full = service.searchDictation(new SearchDictationRequest().setKeyword("1").setType("Article"), 50)
        def summaries = service.searchDictationSummary(
                new SearchDictationRequest().setKeyword("1").setType("Article").setShortPayload(true), 50)
        def missing = service.searchDictationSummary(
                new SearchDictationRequest().setKeyword("9999999").setShortPayload(true), 50)

        then:
        full.size() == 1
        summaries.size() == 1
        summaries[0].id == full[0].id
        summaries[0].type == Dictation.DictationType.Vocab
        summaries[0].type == full[0].type
        summaries[0].questionCount == full[0].vocabs.size()
        summaries[0].title == full[0].title
        summaries[0].suitableStudent == full[0].suitableStudent
        summaries[0].source == full[0].source
        summaries[0].totalAttempt == full[0].totalAttempt
        summaries[0].totalRecommended == full[0].totalRecommended
        missing.isEmpty()
    }

    def "short search questionCount splits article sentences at five words"() {
        given:
        def article = "Victim Jane Tweddle-Taylor a receptionist at South Shore Academy School in Blackpool"
        def dictation = new Dictation("five word split search")
        dictation.article = article
        dictation.description = ""
        dictation.sentenceLength = "Long"
        dictation.creator = testService.tester1
        dictation.source = Dictation.Source.FillIn
        dictation.suitableStudent = Any
        dictationDAO.persist(dictation)
        dictationDAO.flush()
        def savedId = dictation.id

        when:
        def summaries = service.searchDictationSummary(
                new SearchDictationRequest().setKeyword("five word split search").setSearchDescription(false).setShortPayload(true), 50)

        then:
        summaries.size() == 1
        summaries[0].id == savedId
        summaries[0].type == Dictation.DictationType.Article
        summaries[0].questionCount == DictationSentenceChunker.divideToSentences(article, DictationSentenceChunker.WORDS_NORMAL).size()
        summaries[0].questionCount != DictationSentenceChunker.divideToSentences(article, DictationSentenceChunker.WORDS_LONG).size()
        summaries[0].questionCount == 3

        cleanup:
        deleteDictation(savedId)
    }

    private List<Long> searchIds(SearchDictationRequest request) {
        def full = service.searchDictation(request, 50)
        def summaries = service.searchDictationSummary(request, 50)
        assert summaries*.id == full*.id
        return full*.id
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

    def "short search does not load dictation entities or vocabs"() {
        given:
        def statistics = entityManager.entityManagerFactory.unwrap(SessionFactory).statistics
        statistics.statisticsEnabled = true
        statistics.clear()

        when:
        def summaries = service.searchDictationSummary(new SearchDictationRequest().setKeyword("tam").setShortPayload(true), 50)

        then:
        summaries.size() > 1
        statistics.entityLoadCount == 0
        statistics.collectionFetchCount == 0
        statistics.collectionLoadCount == 0
    }

    def dateFrom(String date) {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
}
