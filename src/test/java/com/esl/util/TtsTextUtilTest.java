package com.esl.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TtsTextUtilTest {

    @Test
    void normalizeShouldTrimAndCollapseWhitespace() {
        var input = "  Hello \r\n  world\t\t\n";
        var result = TtsTextUtil.normalize(input);
        assertEquals("Hello world", result);
    }

    @Test
    void normalizeShouldReturnEmptyStringForNull() {
        var result = TtsTextUtil.normalize(null);
        assertEquals("", result);
    }

    @Test
    void punctuationTextShouldReplacePunctuationWhenEnabled() {
        var input = "Hi, world!";
        var result = TtsTextUtil.toPunctuationText(input);
        assertEquals("Hi, comma, world, exclamation mark", result);
    }


    @Test
    void punctuationTextShouldReplaceHyphenWhenEnabled() {
        var input = "alpha-beta";
        var result = TtsTextUtil.toPunctuationText(input);
        assertEquals("alpha, hyphen, beta", result);
    }

    @Test
    void punctuationTextShouldPreserveInternalApostrophes() {
        var input = "don't";
        var result = TtsTextUtil.toPunctuationText(input);
        assertEquals("don't", result);
    }

    @Test
    void punctuationTextShouldSpeakStandaloneApostrophes() {
        var input = "'Hello'";
        var result = TtsTextUtil.toPunctuationText(input);
        assertEquals("apostrophe, Hello, apostrophe", result);
    }

    @Test
    void sha256HexShouldBeDeterministic() {
        var input = "hello";
        var first = TtsTextUtil.sha256Hex(input);
        var second = TtsTextUtil.sha256Hex(input);
        assertEquals(first, second);
    }

}
