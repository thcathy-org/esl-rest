package com.esl.service.tts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TtsAudioKeyBuilderTest {

    @Test
    void buildAudioKey_shouldUseSlugShardSlugMaxAndHash() {
        var key = TtsAudioKeyBuilder.buildAudioKey("v1", "hello world", "abc123");
        assertEquals("tts/v1/he/hello world/abc123.mp3", key);
    }

    @Test
    void buildAudioKey_shouldUseFallbackShardAndSlugForBlankInput() {
        var key = TtsAudioKeyBuilder.buildAudioKey("v1", " ", "abc123");
        assertEquals("tts/v1/__/__/abc123.mp3", key);
    }

    @Test
    void buildAudioKey_shouldTruncateSlugTo40Chars() {
        var normalized = "abcdefghijklmnopqrstuvwxyz0123456789-EXTRA";
        var key = TtsAudioKeyBuilder.buildAudioKey("v1", normalized, "abc123");
        assertEquals("tts/v1/ab/abcdefghijklmnopqrstuvwxyz0123456789-EXT/abc123.mp3", key);
    }
}

