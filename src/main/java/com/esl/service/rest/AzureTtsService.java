package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class AzureTtsService {
    private static final Logger logger = LoggerFactory.getLogger(AzureTtsService.class);

    private final WebClient webClient;
    private final boolean configured;
    private final Duration requestTimeout;

    @Value("${AzureTtsService.Voice:en-US-AvaNeural}")
    private String voice;

    @Value("${AzureTtsService.OutputFormat:audio-24khz-48kbitrate-mono-mp3}")
    private String outputFormat;

    public AzureTtsService(
            @Value("${AZURE_SPEECH_KEY:}") String subscriptionKey,
            @Value("${AzureTtsService.Region:}") String region,
            @Value("${AzureTtsService.RequestTimeoutInSecond:30}") int timeoutSec
    ) {
        var key = StringUtils.trimToNull(subscriptionKey);
        var rgn = StringUtils.trimToNull(region);
        this.requestTimeout = Duration.ofSeconds(timeoutSec);

        if (key == null || rgn == null) {
            logger.warn("AzureTtsService not configured: AZURE_SPEECH_KEY or AzureTtsService.Region missing");
            this.configured = false;
            this.webClient = null;
            return;
        }

        this.configured = true;
        this.webClient = WebClient.builder()
                .baseUrl("https://" + rgn + ".tts.speech.microsoft.com")
                .defaultHeader("Ocp-Apim-Subscription-Key", key)
                .build();
    }

    public boolean isConfigured() {
        return configured;
    }

    public byte[] textToSpeech(String text) {
        if (!configured) {
            throw new IllegalStateException("AzureTtsService is not configured");
        }

        var ssml = buildSsml(text);
        logger.info("Calling Azure TTS voice={} textLen={}", voice, text.length());

        return webClient.post()
                .uri("/cognitiveservices/v1")
                .header("Content-Type", "application/ssml+xml")
                .header("X-Microsoft-OutputFormat", outputFormat)
                .header("User-Agent", "esl-rest")
                .bodyValue(ssml)
                .retrieve()
                .onStatus(s -> s.value() >= 400, this::handleError)
                .bodyToMono(byte[].class)
                .timeout(requestTimeout)
                .block();
    }

    String buildSsml(String text) {
        var safe = (text == null ? "" : text)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        return "<speak version='1.0' xml:lang='en-US'>"
                + "<voice name='" + voice + "'>" + safe + "</voice></speak>";
    }

    private Mono<? extends Throwable> handleError(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException(
                        "Azure TTS error: " + resp.statusCode() + "\nBody: " + body)));
    }
}
