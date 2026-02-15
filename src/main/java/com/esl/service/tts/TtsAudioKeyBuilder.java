package com.esl.service.tts;

import org.apache.commons.lang3.StringUtils;

public final class TtsAudioKeyBuilder {
    private static final int MAX_SLUG_LENGTH = 40;
    private static final String FALLBACK_SEGMENT = "__";
    private static final String SLUG_REPLACEMENT = "-";

    private TtsAudioKeyBuilder() {}

    public static String buildAudioKey(String ttsVersion, String normalizedText, String keyHash) {
        var slug = sanitizeSlug(normalizedText);
        var slugMax = slug.length() > MAX_SLUG_LENGTH ? slug.substring(0, MAX_SLUG_LENGTH) : slug;
        if (StringUtils.isBlank(slugMax)) {
            slugMax = FALLBACK_SEGMENT;
        }
        var shard = slugMax.length() >= 2 ? slugMax.substring(0, 2) : FALLBACK_SEGMENT;
        return String.format("tts/%s/%s/%s/%s.mp3", ttsVersion, shard, slugMax, keyHash);
    }

    private static String sanitizeSlug(String text) {
        var slug = StringUtils.defaultString(text).toLowerCase();
        slug = slug.replaceAll("[^a-z0-9]+", SLUG_REPLACEMENT);
        slug = StringUtils.strip(slug, SLUG_REPLACEMENT);
        return StringUtils.defaultIfBlank(slug, FALLBACK_SEGMENT);
    }
}

