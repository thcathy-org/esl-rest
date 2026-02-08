package com.esl.service.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
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

@Service
public class SpeechWorkerService {
    private static final Logger logger = LoggerFactory.getLogger(SpeechWorkerService.class);
    private static final String API_HEADER_NAME = "X-API-KEY";

    private final WebClient webClient;
    private final Duration requestTimeout;
    private final String apiKey;

    public SpeechWorkerService(
            @Value("${ESL_SPEECH_WORKER_HOST}") String apiHost,
            @Value("${ESL_SPEECH_WORKER_APIKEY}") String apiKey,
            @Value("${SpeechWorkerService.RequestTimeoutInSecond}") int requestTimeoutInSecond
    ) {
        if (StringUtils.isBlank(apiHost)) {
            logger.warn("ESL_SPEECH_WORKER_HOST is not set; speech worker calls will be disabled");
        }
        if (!apiHost.startsWith("http://") && !apiHost.startsWith("https://")) {
            apiHost = "http://" + apiHost;
        }

        this.apiKey = StringUtils.trimToNull(apiKey);
        this.requestTimeout = Duration.ofSeconds(requestTimeoutInSecond);
        
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(apiHost)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (this.apiKey != null) {
            builder.defaultHeader(API_HEADER_NAME, this.apiKey);
        }
        this.webClient = builder.build();
    }

    public GenerateResponse generate(GenerateRequest request) {
        return webClient.post()
                .uri("/generate")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, this::handleErrorResponse)
                .bodyToMono(GenerateResponse.class)
                .timeout(requestTimeout)
                .block();
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    String errorMessage = String.format(
                            "Speech worker call failed: %s\nResponse body: %s",
                            clientResponse.statusCode(),
                            errorBody
                    );
                    return Mono.error(new RuntimeException(errorMessage));
                });
    }

    public static class GenerateRequest {
        public String text;
        public String voice;
        public Double speed;
        @JsonProperty("audio_format")
        public String audioFormat;
        @JsonProperty("mp3_quality")
        public Integer mp3Quality;
    }

    public static class GenerateResponse {
        @JsonProperty("audio_base64")
        public String audioBase64;
        @JsonProperty("audio_format")
        public String audioFormat;
        @JsonProperty("mime_type")
        public String mimeType;
        @JsonProperty("sample_rate")
        public Integer sampleRate;
        @JsonProperty("word_timestamps")
        public Object wordTimestamps;
        @JsonProperty("original_text")
        public String originalText;
        @JsonProperty("processed_text")
        public String processedText;
    }
}
