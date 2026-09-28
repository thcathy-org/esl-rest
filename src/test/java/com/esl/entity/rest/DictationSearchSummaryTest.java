package com.esl.entity.rest;

import com.esl.entity.dictation.Dictation;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DictationSearchSummaryTest {

    @Test
    void fromSearchRow_countsVocabsWhenArticleIsBlank() {
        var summary = DictationSearchSummary.fromSearchRow(
                1L, "Testing 1", new Date(), Dictation.StudentLevel.Any,
                4, 2, Dictation.Source.FillIn, "  ", 3);

        assertEquals(Dictation.DictationType.Vocab, summary.getType());
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

        assertEquals(Dictation.DictationType.Article, summary.getType());
        assertEquals(3, summary.getQuestionCount());
    }

    @Test
    void fromDictation_matchesGetType() {
        var dictation = new Dictation("Sentence");
        dictation.setId(3L);
        dictation.setArticle("One two three four five.");
        dictation.setVocabs(null);

        var summary = DictationSearchSummary.fromDictation(dictation);

        assertEquals(dictation.getType(), summary.getType());
        assertEquals(Dictation.DictationType.Article, summary.getType());
        assertEquals(1, summary.getQuestionCount());
        assertEquals(3L, summary.getId());
    }
}
