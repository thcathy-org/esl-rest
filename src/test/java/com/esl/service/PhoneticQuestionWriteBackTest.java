package com.esl.service;

import com.esl.dao.PhoneticQuestionDAO;
import com.esl.entity.rest.DictionaryResult;
import com.esl.enumeration.VocabDifficulty;
import com.esl.model.PhoneticQuestion;
import com.esl.service.rest.WebParserRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties",
        properties = "spring.datasource.url=jdbc:h2:mem:writeback;Mode=MySQL;DB_CLOSE_DELAY=-1")
class PhoneticQuestionWriteBackTest {

    @Autowired VocabService vocabService;
    @Autowired PhoneticQuestionDAO phoneticQuestionDAO;
    @MockBean WebParserRestService webService;

    @BeforeEach
    void emptyDictionaryByDefault() {
        when(webService.queryDictionary(anyString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
    }

    @Test
    void missThenSecondRequestHitsDbAndSkipsDictionary() {
        dictionary("serendipity", "serendipity", "sɛrənˈdɪpɪti", "https://example.com/serendipity.mp3");

        var first = vocabService.createQuestion("serendipity");
        dictionary("serendipity", "serendipity", "changed", "https://example.com/changed.mp3");
        var second = vocabService.createQuestion("serendipity");

        verify(webService, times(1)).queryDictionary("serendipity");
        assertEquals("sɛrənˈdɪpɪti", first.getIPA());
        assertEquals("sɛrənˈdɪpɪti", second.getIPA());
        assertEquals("https://example.com/serendipity.mp3", second.getPronouncedLink());
        assertArrayEquals(new String[] { "" }, first.getPicsFullPaths());
        assertArrayEquals(new String[] { "" }, second.getPicsFullPaths());

        var row = phoneticQuestionDAO.getPhoneticQuestionByWord("serendipity");
        assertEquals("serendipity", row.getWord());
        assertEquals("sɛrənˈdɪpɪti", row.getIPA());
        assertEquals("https://example.com/serendipity.mp3", row.getPronouncedLink());
        assertEquals(0, row.getRank());
        assertEquals(0d, row.getFrequency());
        assertNull(row.getPicFileName());
        assertNull(row.getPronouncedLinkBackup());
        assertTrue(row.getGrades().isEmpty());
        assertEquals(1, phoneticQuestionDAO.countByWord("serendipity"));
    }

    @Test
    void persistsTheRequestedWordWithoutChangingCase() {
        dictionary("Serendipity", "serendipity", "sɛrənˈdɪpɪti", "https://example.com/serendipity.mp3");

        vocabService.createQuestion("Serendipity");

        var row = phoneticQuestionDAO.getPhoneticQuestionByWord("Serendipity");
        assertNotNull(row);
        assertEquals("Serendipity", row.getWord());
        assertEquals(0, row.getRank());
    }

    @Test
    void incompleteDictionaryResultsDoNotInsert() {
        dictionary("ipa-only", "ipa-only", "aɪ", null);
        dictionary("url-only", "url-only", null, "https://example.com/url-only.mp3");
        dictionary("blank-ipa", "blank-ipa", "  ", "https://example.com/blank-ipa.mp3");
        dictionary("blank-url", "blank-url", "aɪ", "  ");
        when(webService.queryDictionary("missing"))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        dictionary("running", "run", "rʌn", "https://example.com/run.mp3");

        assertFalse(vocabService.createQuestion("ipa-only").enriched());
        assertFalse(vocabService.createQuestion("url-only").enriched());
        assertFalse(vocabService.createQuestion("blank-ipa").enriched());
        assertFalse(vocabService.createQuestion("blank-url").enriched());
        assertFalse(vocabService.createQuestion("missing").enriched());
        var mismatch = vocabService.createQuestion("running");
        assertNull(mismatch.getPronouncedLink());
        assertTrue(mismatch.isIPAUnavailable());

        assertEquals(0, phoneticQuestionDAO.countByWord("ipa-only"));
        assertEquals(0, phoneticQuestionDAO.countByWord("url-only"));
        assertEquals(0, phoneticQuestionDAO.countByWord("blank-ipa"));
        assertEquals(0, phoneticQuestionDAO.countByWord("blank-url"));
        assertEquals(0, phoneticQuestionDAO.countByWord("missing"));
        assertEquals(0, phoneticQuestionDAO.countByWord("running"));
    }

    @Test
    void sparseExistingRowIsReturnedUnchanged() {
        var sparse = new PhoneticQuestion();
        sparse.setWord("halfbaked");
        sparse.setIPA("hɑːf");
        sparse.setPronouncedLink(null);
        sparse.setRank(12);
        phoneticQuestionDAO.persist(sparse);
        dictionary("halfbaked", "halfbaked", "hɑːfbeɪkt", "https://example.com/halfbaked.mp3");

        var loaded = vocabService.createQuestion("halfbaked");

        verify(webService, never()).queryDictionary("halfbaked");
        assertEquals("hɑːf", loaded.getIPA());
        assertNull(loaded.getPronouncedLink());
        assertEquals(12, loaded.getRank());
        assertArrayEquals(new String[] { "" }, loaded.getPicsFullPaths());

        var row = phoneticQuestionDAO.getPhoneticQuestionByWord("halfbaked");
        assertEquals("hɑːf", row.getIPA());
        assertNull(row.getPronouncedLink());
        assertEquals(12, row.getRank());
    }

    @Test
    void existingBankRowKeepsPictureHideAndDoesNotCallDictionary() {
        var apple = vocabService.createQuestion("apple");

        verify(webService, never()).queryDictionary("apple");
        assertEquals("ˋæpl", apple.getIPA());
        assertArrayEquals(new String[] { "" }, apple.getPicsFullPaths());
        assertEquals("FRUT_032.jpg", phoneticQuestionDAO.getPhoneticQuestionByWord("apple").getPicFileName());
    }

    @Test
    void generatePracticeNeverReturnsRankZeroWriteBack() {
        dictionary("rankzero", "rankzero", "ræŋk", "https://example.com/rankzero.mp3");
        var written = vocabService.createQuestion("rankzero");
        assertEquals(0, written.getRank());

        for (var difficulty : VocabDifficulty.values()) {
            var practice = vocabService.generatePractice(difficulty);
            assertEquals(3, practice.getVocabs().size());
            for (var vocab : practice.getVocabs()) {
                assertNotEquals("rankzero", vocab.getWord());
                assertTrue(phoneticQuestionDAO.getPhoneticQuestionByWord(vocab.getWord()).getRank() >= 1);
            }
        }
    }

    @Test
    void uniqueWordRejectsASecondInsert() {
        var first = slim("dupword");
        phoneticQuestionDAO.insertIfAbsent(first);

        var second = slim("dupword");
        assertThrows(DataIntegrityViolationException.class, () -> phoneticQuestionDAO.persist(second));
        assertEquals(1, phoneticQuestionDAO.countByWord("dupword"));
    }

    @Test
    void concurrentDoubleMissInsertsOneRow() throws Exception {
        dictionary("raceword", "raceword", "reɪs", "https://example.com/raceword.mp3");
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(2);
        var failures = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(2);
        for (var i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    var question = vocabService.createQuestion("raceword");
                    assertEquals("reɪs", question.getIPA());
                    assertEquals("https://example.com/raceword.mp3", question.getPronouncedLink());
                    assertArrayEquals(new String[] { "" }, question.getPicsFullPaths());
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(20, TimeUnit.SECONDS));
        pool.shutdown();
        assertEquals(0, failures.get());
        assertEquals(1, phoneticQuestionDAO.countByWord("raceword"));
        verify(webService, atLeastOnce()).queryDictionary(eq("raceword"));
    }

    private void dictionary(String requested, String resolved, String ipa, String url) {
        when(webService.queryDictionary(requested)).thenReturn(CompletableFuture.completedFuture(
                Optional.of(new DictionaryResult(resolved, url, "en-US", ipa, "definition"))));
    }

    private static PhoneticQuestion slim(String word) {
        var question = new PhoneticQuestion();
        question.setWord(word);
        question.setIPA("ipa");
        question.setPronouncedLink("https://example.com/" + word + ".mp3");
        question.setRank(0);
        return question;
    }
}
