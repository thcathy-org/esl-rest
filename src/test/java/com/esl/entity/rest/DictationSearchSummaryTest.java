package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DictationSearchSummaryTest {

    @Test
    void fromSearchRow_countsVocabsWhenArticleIsBlank() {
        var summary = DictationSearchSummary.fromSearchRow(
                1L, "Testing 1", new Date(), Dictation.StudentLevel.Any,
                4, 2, Dictation.Source.FillIn, "  ", 3);

        assertFalse(summary.isSentenceDictation());
        assertEquals(3, summary.getQuestionCount());
        assertEquals(4, summary.getTotalAttempt());
        assertEquals(2, summary.getTotalRecommended());
    }

    @Test
    void fromSearchRow_splitsSentencesAtFiveWords() {
        var article = "Victim Jane Tweddle-Taylor a receptionist at South Shore Academy School in Blackpool";

        var summary = DictationSearchSummary.fromSearchRow(
                9L, "Long", new Date(), Dictation.StudentLevel.JuniorPrimary,
                0, 0, Dictation.Source.FillIn, article, 99);

        assertTrue(summary.isSentenceDictation());
        assertEquals(3, summary.getQuestionCount());
    }
}
