package com.esl.service.tts;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class DictationSentenceChunker {
    public static final int WORDS_SHORT = 3;
    public static final int WORDS_NORMAL = 5;
    public static final int WORDS_LONG = 7;
    public static final int WORDS_VERY_LONG = 10;
    public static final List<Integer> ALL_PRESET_WORD_COUNTS = List.of(
            WORDS_SHORT,
            WORDS_NORMAL,
            WORDS_LONG,
            WORDS_VERY_LONG
    );

    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("(?<=[.?!])\\s+");
    private static final String SENTENCE_LEN_SHORT = "Short";
    private static final String SENTENCE_LEN_LONG = "Long";
    private static final String SENTENCE_LEN_VERY_LONG = "VeryLong";

    private DictationSentenceChunker() {}

    public static int sentenceLengthOptionToWordCount(String sentenceLengthOption) {
        if (SENTENCE_LEN_SHORT.equals(sentenceLengthOption)) {
            return WORDS_SHORT;
        } else if (SENTENCE_LEN_LONG.equals(sentenceLengthOption)) {
            return WORDS_LONG;
        } else if (SENTENCE_LEN_VERY_LONG.equals(sentenceLengthOption)) {
            return WORDS_VERY_LONG;
        } else {
            return WORDS_NORMAL;
        }
    }

    public static List<String> divideToSentences(String article, int maxWordsInSentence) {
        if (StringUtils.isBlank(article)) {
            return List.of();
        }

        var results = new ArrayList<String>();
        for (var line : article.split("\n")) {
            var cleanedLine = line.replace("\t", "").trim();
            for (var sentence : splitBySentenceEnding(cleanedLine)) {
                results.addAll(splitLongSentenceByWords(sentence, maxWordsInSentence));
            }
        }

        return results.stream()
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    static List<String> splitBySentenceEnding(String input) {
        if (StringUtils.isBlank(input)) {
            return List.of();
        }

        return SENTENCE_SPLIT_PATTERN.splitAsStream(input)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    static List<String> splitLongSentenceByWords(String input, int maxWords) {
        var words = input.split(" ");
        var nonEmptyWordCount = 0;
        for (var word : words) {
            if (!word.isEmpty()) {
                nonEmptyWordCount++;
            }
        }

        if (nonEmptyWordCount <= maxWords) {
            return List.of(input);
        }

        return splitLongLineBySpace(input, maxWords);
    }

    static List<String> splitLongLineBySpace(String input, int maxWordsInSentence) {
        var strings = input.split(" ");
        if (strings.length <= maxWordsInSentence) {
            return List.of(input);
        }

        var results = new ArrayList<String>();
        var endIndex = maxWordsInSentence;
        while (endIndex + maxWordsInSentence <= strings.length) {
            results.add(String.join(" ", List.of(strings).subList(endIndex - maxWordsInSentence, endIndex)));
            endIndex += maxWordsInSentence;
        }

        if (endIndex == strings.length) {
            results.add(String.join(" ", List.of(strings).subList(endIndex - maxWordsInSentence, endIndex)));
        } else {
            // Match Ionic behavior: evenly split the remaining words into last two chunks.
            var startIndex = endIndex - maxWordsInSentence;
            endIndex = startIndex + (int) Math.ceil((strings.length - startIndex) / 2.0);
            results.add(String.join(" ", List.of(strings).subList(startIndex, endIndex)));
            results.add(String.join(" ", List.of(strings).subList(endIndex, strings.length)));
        }

        return results;
    }
}

