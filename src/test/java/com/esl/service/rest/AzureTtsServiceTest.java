package com.esl.service.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AzureTtsServiceTest {

    @Test
    void isConfigured_returnsFalseWhenKeyMissing() {
        var service = new AzureTtsService("", "eastasia", 30);
        assertFalse(service.isConfigured());
    }

    @Test
    void isConfigured_returnsFalseWhenRegionMissing() {
        var service = new AzureTtsService("key", "", 30);
        assertFalse(service.isConfigured());
    }

    @Test
    void isConfigured_returnsTrueWhenBothSet() {
        var service = new AzureTtsService("key", "eastasia", 30);
        assertTrue(service.isConfigured());
    }

    @Test
    void textToSpeech_throwsWhenNotConfigured() {
        var service = new AzureTtsService("", "", 30);
        var ex = assertThrows(IllegalStateException.class, () -> service.textToSpeech("hello"));
        assertTrue(ex.getMessage().contains("not configured"));
    }

    @ParameterizedTest(name = "buildSsml escapes [{0}]")
    @MethodSource("ssmlEscapeCases")
    void buildSsml_escapesXmlSpecialCharacters(String input, String expectedFragment) {
        var service = new AzureTtsService("key", "eastasia", 30);
        ReflectionTestUtils.setField(service, "voice", "en-US-AvaNeural");

        var ssml = service.buildSsml(input);

        assertTrue(ssml.contains(expectedFragment),
                "expected SSML to contain '" + expectedFragment + "' but was: " + ssml);
        assertTrue(ssml.startsWith("<speak version='1.0' xml:lang='en-US'>"));
        assertTrue(ssml.contains("<voice name='en-US-AvaNeural'>"));
        assertTrue(ssml.endsWith("</voice></speak>"));
    }

    static Stream<Arguments> ssmlEscapeCases() {
        return Stream.of(
                Arguments.of("Tom & Jerry", "Tom &amp; Jerry"),
                Arguments.of("5 < 10", "5 &lt; 10"),
                Arguments.of("a > b", "a &gt; b"),
                Arguments.of("<script>alert(1)</script>", "&lt;script&gt;alert(1)&lt;/script&gt;"),
                Arguments.of("plain text", "plain text")
        );
    }

    @Test
    void buildSsml_handlesNullInput() {
        var service = new AzureTtsService("key", "eastasia", 30);
        ReflectionTestUtils.setField(service, "voice", "en-US-AvaNeural");

        var ssml = service.buildSsml(null);

        assertTrue(ssml.contains("<voice name='en-US-AvaNeural'></voice>"));
    }

    @Test
    void buildSsml_usesConfiguredVoice() {
        var service = new AzureTtsService("key", "eastasia", 30);
        ReflectionTestUtils.setField(service, "voice", "en-GB-SoniaNeural");

        var ssml = service.buildSsml("Hello");

        assertTrue(ssml.contains("<voice name='en-GB-SoniaNeural'>Hello</voice>"));
    }
}
