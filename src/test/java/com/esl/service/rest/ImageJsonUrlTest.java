package com.esl.service.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageJsonUrlTest {

    @Test
    void publicUrl_matchesImageGenerationServerPath() {
        var host = "https://firebasestorage.googleapis.com/v0/b/friendlyeats-7fa2f.appspot.com/o/images";
        assertEquals(host + "%2Fap%2Fapple.json?alt=media", ImageJsonUrl.publicUrl(host, "Apple"));
        assertEquals(
                host + "%2Fth%2Fthe-is-not-a-----correct-phrase.json?alt=media",
                ImageJsonUrl.publicUrl(host + "/", "The is not a !@# correct phrase"));
    }
}
