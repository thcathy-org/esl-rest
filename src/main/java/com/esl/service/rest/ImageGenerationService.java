package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ImageGenerationService {
    final static String API_HEADER_NAME = "X-API-KEY";
    final static Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);

    final WebClient webClient;
    final Duration requestTimeout;
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    /** Phrases currently queued or in-flight; prevents amend floods from re-enqueuing duplicates. */
    private final Set<String> pendingPhrases = ConcurrentHashMap.newKeySet();

    public ImageGenerationService(
            @Value("${IMAGE_GENERATION_SERVER_HOST:test_value}") String apiHost,
            @Value("${IMAGE_GENERATION_SERVER_APIKEY:test_value}") String apiKey,
            @Value("${ImageGenerationService.RequestTimeoutInSecond}") int requestTimeoutInSecond)
    {
        if (StringUtils.isBlank(apiHost)) throw new IllegalArgumentException("Cannot create ImageGenerationService without API host");
        if (StringUtils.isBlank(apiKey)) throw new IllegalArgumentException("Cannot create ImageGenerationService without API key");
        if (!apiHost.startsWith("http://") && !apiHost.startsWith("https://")) apiHost = "http://" + apiHost;

        this.requestTimeout = Duration.ofSeconds(requestTimeoutInSecond);
        webClient = WebClient.builder()
                .baseUrl(apiHost)
                .defaultHeader(API_HEADER_NAME, apiKey)
                .build();

        Executors.newSingleThreadExecutor().submit(this::submitRequestTask);
    }

    public void submitRequest(String phrase) {
        if (StringUtils.isBlank(phrase)) {
            return;
        }
        String key = phrase.trim().toLowerCase(Locale.ROOT);
        if (!pendingPhrases.add(key)) {
            logger.info("Skip duplicate image request '{}'", phrase);
            return;
        }
        queue.add(phrase.trim());
        logger.info("Added '{}' to queue", phrase);
    }

    private void submitRequestTask() {
        while (true) {
            while (!queue.isEmpty()) {
                String phrase = queue.poll();
                String key = phrase == null ? null : phrase.trim().toLowerCase(Locale.ROOT);
                try {
                    generate(phrase).block(requestTimeout);
                    if (key != null) {
                        pendingPhrases.remove(key);
                    }
                } catch (Exception e) {
                    sleep(5);
                    logger.warn("Exception when submitting, retry later", e);
                    queue.add(phrase);
                }
            }
            sleep(1);
        }
    }

    private void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    public Mono<ResponseEntity<Void>> generate(String phrase) {
        logger.info("submit request: {}", phrase);
        return webClient.post().uri("/image/generate/" + phrase).retrieve().toBodilessEntity();
    }

}
