package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class FishSpeechService {
    private static final Logger logger = LoggerFactory.getLogger(FishSpeechService.class);

    private final WebClient webClient;
    private final boolean configured;
    private final Duration requestTimeout;

    public FishSpeechService(
            @Value("${FISH_SPEECH_URL:}") String url,
            @Value("${FISH_SPEECH_USERNAME:}") String username,
            @Value("${FISH_SPEECH_PASSWORD:}") String password,
            @Value("${FishSpeechService.RequestTimeoutInSecond:120}") int timeoutSec
    ) {
        var trimmedUrl = StringUtils.trimToNull(url);
        var trimmedUser = StringUtils.trimToNull(username);
        var trimmedPass = StringUtils.trimToNull(password);
        this.requestTimeout = Duration.ofSeconds(timeoutSec);

        if (trimmedUrl == null || trimmedUser == null || trimmedPass == null) {
            logger.warn("FishSpeechService not configured: FISH_SPEECH_URL, FISH_SPEECH_USERNAME or FISH_SPEECH_PASSWORD missing");
            this.configured = false;
            this.webClient = null;
            return;
        }

        this.configured = true;
        this.webClient = WebClient.builder()
                .baseUrl(trimmedUrl)
                .defaultHeaders(h -> h.setBasicAuth(trimmedUser, trimmedPass))
                .build();
    }

    public boolean isConfigured() {
        return configured;
    }

    public byte[] textToSpeech(String text) {
        if (!configured) {
            throw new IllegalStateException("FishSpeechService is not configured");
        }

        logger.info("Calling Fish Speech TTS for textLen={}", text.length());
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("text", text, "format", "mp3"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Fish Speech error: " + resp.statusCode() + " body: " + body))))
                .bodyToMono(byte[].class)
                .timeout(requestTimeout)
                .block();
    }
}
