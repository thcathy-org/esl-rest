package com.esl.service.rest;

import java.util.Locale;

public final class ImageJsonUrl {
    private ImageJsonUrl() {}

    public static String publicUrl(String imagesHost, String phrase) {
        var filename = phrase.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z]", "-") + ".json";
        var folder = filename.length() < 2 ? filename : filename.substring(0, 2);
        var host = imagesHost.endsWith("/") ? imagesHost.substring(0, imagesHost.length() - 1) : imagesHost;
        return host + "%2F" + folder + "%2F" + filename + "?alt=media";
    }
}
