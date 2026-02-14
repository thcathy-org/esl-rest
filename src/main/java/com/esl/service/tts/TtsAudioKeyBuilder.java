package com.esl.service.tts;

import org.apache.commons.lang3.StringUtils;

public final class TtsAudioKeyBuilder {
    private static final int MAX_SLUG_LENGTH = 40;
    private static final String FALLBACK_SEGMENT = "__";

    private TtsAudioKeyBuilder() {}

    public static String buildAudioKey(String ttsVersion, String normalizedText, String keyHash) {
        var slug = StringUtils.defaultString(normalizedText);
        var shard = slug.length() >= 2 ? slug.substring(0, 2) : FALLBACK_SEGMENT;
        var slugMax = slug.length() > MAX_SLUG_LENGTH ? slug.substring(0, MAX_SLUG_LENGTH) : slug;
        if (StringUtils.isBlank(slugMax)) {
            slugMax = FALLBACK_SEGMENT;
        }
        return String.format("tts/%s/%s/%s/%s.mp3", ttsVersion, shard, slugMax, keyHash);
    }
}

