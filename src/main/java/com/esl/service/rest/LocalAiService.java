package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;

@Service
public class LocalAiService {
    private static final Logger logger = LoggerFactory.getLogger(LocalAiService.class);

    private final WebClient webClient;
    private final boolean configured;
    private final Duration ttsRequestTimeout;
    private final String ttsModel;
    private final String ttsDefaultVoice;

    public LocalAiService(
            @Value("${LOCALAI_URL:}") String baseUrl,
            @Value("${LOCALAI_API_KEY:}") String apiKey,
            @Value("${LocalAiService.TtsModel:kokoro}") String ttsModel,
            @Value("${LocalAiService.TtsVoice:}") String ttsDefaultVoice,
            @Value("${LocalAiService.TtsRequestTimeoutInSecond:120}") int ttsTimeoutSec
    ) {
        baseUrl = StringUtils.trimToNull(baseUrl);
        apiKey = StringUtils.trimToNull(apiKey);
        this.ttsRequestTimeout = Duration.ofSeconds(ttsTimeoutSec);
        this.ttsModel = StringUtils.defaultIfBlank(ttsModel, "kokoro");
        this.ttsDefaultVoice = StringUtils.trimToNull(ttsDefaultVoice);

        if (baseUrl == null || apiKey == null) {
            logger.warn("LocalAiService not configured: LOCALAI_URL or LOCALAI_API_KEY missing");
            this.configured = false;
            this.webClient = null;
            return;
        }

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }
        baseUrl = StringUtils.removeEnd(baseUrl, "/");

        this.configured = true;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public boolean isConfigured() {
        return configured;
    }

    public byte[] textToSpeech(String text) {
        return textToSpeech(text, null);
    }

    public byte[] textToSpeech(String text, String voiceOverride) {
        requireConfigured();

        var body = new LinkedHashMap<String, Object>();
        body.put("model", ttsModel);
        body.put("input", text);
        body.put("response_format", "mp3");

        var voice = StringUtils.trimToNull(voiceOverride);
        if (voice == null) {
            voice = ttsDefaultVoice;
        }
        if (voice != null) {
            body.put("voice", voice);
        }

        logger.info("Calling LocalAI TTS model={} textLen={}", ttsModel, text.length());
        return webClient.post()
                .uri("/v1/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException(
                                        "LocalAI TTS error: " + resp.statusCode() + " body: " + err))))
                .bodyToMono(byte[].class)
                .timeout(ttsRequestTimeout)
                .block();
    }

    private void requireConfigured() {
        if (!configured) {
            throw new IllegalStateException("LocalAiService is not configured");
        }
    }
}
