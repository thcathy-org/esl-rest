package com.esl.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class TtsTextUtil {
    private TtsTextUtil() {}

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        var text = input.replace("\r\n", "\n").trim();
        text = text.replaceAll("\\s+", " ");
        return text;
    }

    public static String toPunctuationText(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        var text = input;
        text = text.replaceAll(",", ", comma,");
        text = text.replaceAll("\\.{3,}|…", ", ellipsis,");
        text = text.replaceAll("\\.", ", full stop,");
        text = text.replaceAll("\\?", ", question mark,");
        text = text.replaceAll("!", ", exclamation mark,");
        text = text.replaceAll(":", ", colon,");
        text = text.replaceAll(";", ", semicolon,");
        text = text.replaceAll("\\(", ", open round bracket,");
        text = text.replaceAll("\\)", ", close round bracket,");
        text = text.replaceAll("\\[", ", open square bracket,");
        text = text.replaceAll("]", ", close square bracket,");
        text = text.replaceAll("/", ", slash,");
        text = text.replaceAll("\"|“|”", ", double quote,");
        text = text.replaceAll("-|—", ", hyphen,");

        text = replaceApostrophes(text);

        var tokens = new ArrayList<String>();
        for (var token : text.split(",")) {
            var cleaned = token.replaceAll("\\s+", " ").trim();
            if (!cleaned.isEmpty()) {
                tokens.add(cleaned);
            }
        }
        return String.join(", ", tokens);
    }

    public static String sha256Hex(String input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder(hash.length * 2);
            for (var b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    private static String replaceApostrophes(String input) {
        var sb = new StringBuilder();
        for (var i = 0; i < input.length(); i++) {
            var ch = input.charAt(i);
            if (ch == '\'' || ch == '’' || ch == '‘') {
                var prev = i > 0 ? input.charAt(i - 1) : 0;
                var next = i + 1 < input.length() ? input.charAt(i + 1) : 0;
                var prevIsAlphaNum = prev != 0 && Character.isLetterOrDigit(prev);
                var nextIsAlphaNum = next != 0 && Character.isLetterOrDigit(next);
                if (prevIsAlphaNum && nextIsAlphaNum) {
                    sb.append(ch);
                } else {
                    sb.append(", apostrophe,");
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
