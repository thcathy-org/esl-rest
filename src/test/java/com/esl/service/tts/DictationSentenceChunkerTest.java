package com.esl.service.tts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DictationSentenceChunkerTest {

    @Test
    void sentenceLengthOptionToWordCount_shouldMatchIonicOptions() {
        assertEquals(3, DictationSentenceChunker.sentenceLengthOptionToWordCount("Short"));
        assertEquals(5, DictationSentenceChunker.sentenceLengthOptionToWordCount("Normal"));
        assertEquals(7, DictationSentenceChunker.sentenceLengthOptionToWordCount("Long"));
        assertEquals(10, DictationSentenceChunker.sentenceLengthOptionToWordCount("VeryLong"));
        assertEquals(5, DictationSentenceChunker.sentenceLengthOptionToWordCount(null));
    }

    @Test
    void divideToSentences_shouldSplitBySentenceBoundaryBeforeWordCount() {
        var article = "The cat sat on the mat. The dog ran fast.";

        var results = DictationSentenceChunker.divideToSentences(article, 3);

        assertEquals(List.of(
                "The cat sat",
                "on the mat.",
                "The dog",
                "ran fast."
        ), results);
    }

    @Test
    void divideToSentences_shouldEvenlySplitTailChunks() {
        var sentence = "Victim Jane Tweddle-Taylor a receptionist at South Shore Academy School in Blackpool";

        var results = DictationSentenceChunker.divideToSentences(sentence, 5);

        assertEquals(List.of(
                "Victim Jane Tweddle-Taylor a receptionist",
                "at South Shore Academy",
                "School in Blackpool"
        ), results);
    }

    @Test
    void divideToSentences_shouldRemoveTabsTrimLinesAndIgnoreBlankLines() {
        var article = "\tHello world.\n\n\t  How are you?\t\n";

        var results = DictationSentenceChunker.divideToSentences(article, 5);

        assertEquals(List.of(
                "Hello world.",
                "How are you?"
        ), results);
    }
}

