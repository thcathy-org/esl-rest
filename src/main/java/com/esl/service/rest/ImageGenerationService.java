package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class ImageGenerationService {
    final static String API_HEADER_NAME = "X-API-KEY";
    final static Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);

    final WebClient webClient;
    final Duration requestTimeout;
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    public ImageGenerationService(String apiHost, String apiKey, Duration requestTimeout)
    {
        if (StringUtils.isBlank(apiHost)) throw new IllegalArgumentException("Cannot create ImageGenerationService without API host");
        if (StringUtils.isBlank(apiKey)) throw new IllegalArgumentException("Cannot create ImageGenerationService without API key");
        if (!apiHost.startsWith("http://") && !apiHost.startsWith("https://")) apiHost = "http://" + apiHost;

        this.requestTimeout = requestTimeout;
        webClient = WebClient.builder()
                .baseUrl(apiHost)
                .defaultHeader(API_HEADER_NAME, apiKey)
                .build();

        Executors.newSingleThreadExecutor().submit(this::submitRequestTask);
    }

    public void submitRequest(String phrase) {
        queue.add(phrase);
        logger.info("Added '{}' to queue", phrase);
    }

    private void submitRequestTask() {
        while (true) {
            while (!queue.isEmpty()) {
                String phrase = queue.poll();
                try {
                    generate(phrase).block(requestTimeout);
                } catch (Exception e) {
                    logger.warn("Exception when submitting, retry later", e);
                    queue.add(phrase);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Mono<ResponseEntity<Void>> generate(String phrase) {
        logger.info("submit request: {}", phrase);
        return webClient.post().uri("/generate/" + phrase).retrieve().toBodilessEntity();
    }

}
