package com.esl.service.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalAiServiceTest {

    @Test
    void isConfigured_returnsFalseWhenUrlMissing() {
        var service = new LocalAiService("", "api-key", "kokoro", "af_heart", 60);
        assertFalse(service.isConfigured());
    }

    @Test
    void isConfigured_returnsFalseWhenApiKeyMissing() {
        var service = new LocalAiService("https://local-ai.example", "", "kokoro", "af_heart", 60);
        assertFalse(service.isConfigured());
    }

    @Test
    void isConfigured_returnsTrueWhenBothSet() {
        var service = new LocalAiService("https://local-ai.example", "api-key", "kokoro", "af_heart", 60);
        assertTrue(service.isConfigured());
    }

    @Test
    void isConfigured_addsHttpsSchemeWhenMissing() {
        var service = new LocalAiService("local-ai.example", "api-key", "kokoro", "af_heart", 60);
        assertTrue(service.isConfigured());
    }

    @Test
    void textToSpeech_throwsWhenNotConfigured() {
        var service = new LocalAiService("", "", "kokoro", "af_heart", 60);
        var ex = assertThrows(IllegalStateException.class, () -> service.textToSpeech("hello"));
        assertTrue(ex.getMessage().contains("not configured"));
    }

    @Test
    void textToSpeechWithVoiceOverride_throwsWhenNotConfigured() {
        var service = new LocalAiService("", "", "kokoro", "af_heart", 60);
        var ex = assertThrows(IllegalStateException.class, () -> service.textToSpeech("hello", "af_heart"));
        assertTrue(ex.getMessage().contains("not configured"));
    }
}
