package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class CloudflareAIService {
    private static final Logger logger = LoggerFactory.getLogger(CloudflareAIService.class);
    private static final String BASE_URL = "https://api.cloudflare.com/client/v4";

    private final WebClient webClient;
    private final Duration requestTimeout;
    private final String accountId;
    private final boolean configured;

    @Value("${CloudflareAIService.Aura2.Speaker:luna}")
    private String aura2Speaker;

    @Value("${CloudflareAIService.Aura2.Encoding:mp3}")
    private String aura2Encoding;

    @Value("${CloudflareAIService.Aura2.BitRate:48000}")
    private int aura2BitRate;

    public CloudflareAIService(
            @Value("${CLOUDFLARE_ACCOUNT_ID:}") String accountId,
            @Value("${CLOUDFLARE_API_TOKEN:}") String apiToken,
            @Value("${CloudflareAIService.RequestTimeoutInSecond:30}") int requestTimeoutInSecond
    ) {
        this.accountId = StringUtils.trimToNull(accountId);
        var token = StringUtils.trimToNull(apiToken);

        if (this.accountId == null || token == null) {
            logger.warn("Cloudflare AI not configured: CLOUDFLARE_ACCOUNT_ID or CLOUDFLARE_API_TOKEN is missing");
            this.configured = false;
            this.webClient = null;
            this.requestTimeout = null;
            return;
        }

        this.configured = true;
        this.requestTimeout = Duration.ofSeconds(requestTimeoutInSecond);
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public boolean isConfigured() {
        return configured;
    }

    public byte[] textToSpeech(String text) {
        if (!configured) {
            throw new IllegalStateException("CloudflareAIService is not configured");
        }

        var uri = String.format("/accounts/%s/ai/run/@cf/deepgram/aura-2-en", accountId);
        var body = Map.of(
                "text", text,
                "speaker", aura2Speaker,
                "encoding", aura2Encoding,
                "bit_rate", aura2BitRate
        );

        return webClient.post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() >= 400, this::handleErrorResponse)
                .bodyToMono(byte[].class)
                .timeout(requestTimeout)
                .block();
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    var errorMessage = String.format(
                            "Cloudflare AI call failed: %s\nResponse body: %s",
                            clientResponse.statusCode(),
                            errorBody
                    );
                    return Mono.error(new RuntimeException(errorMessage));
                });
    }

    public static class ErrorResponse {
        public boolean success;
        public java.util.List<ErrorDetail> errors;
        public java.util.List<String> messages;
    }

    public static class ErrorDetail {
        public int code;
        public String message;
    }
}
