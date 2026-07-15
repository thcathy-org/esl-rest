package com.esl.service.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class TelegramNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final WebClient webClient;
    private final String chatId;
    private final boolean configured;
    private final Duration requestTimeout;

    record TelegramApiResponse(boolean ok, String description) {}

    public TelegramNotificationService(
            @Value("${TELEGRAM_BOT_TOKEN:}") String botToken,
            @Value("${TELEGRAM_CHAT_ID:}") String chatId,
            @Value("${TelegramNotificationService.RequestTimeoutInSecond:10}") int timeoutSec
    ) {
        var trimmedToken = StringUtils.trimToNull(botToken);
        var trimmedChatId = StringUtils.trimToNull(chatId);
        this.chatId = trimmedChatId;
        this.requestTimeout = Duration.ofSeconds(timeoutSec);

        if (trimmedToken == null || trimmedChatId == null) {
            logger.warn("TelegramNotificationService not configured: TELEGRAM_BOT_TOKEN or TELEGRAM_CHAT_ID missing");
            this.configured = false;
            this.webClient = null;
            return;
        }

        this.configured = true;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.telegram.org/bot" + trimmedToken)
                .build();
    }

    public boolean isConfigured() {
        return configured;
    }

    public void sendMessage(String text) {
        if (!configured) {
            throw new IllegalStateException("TelegramNotificationService is not configured");
        }

        var response = webClient.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "chat_id", chatId,
                        "text", text
                ))
                .retrieve()
                .bodyToMono(TelegramApiResponse.class)
                .block(requestTimeout);
        requireOkResponse(response);
    }

    static void requireOkResponse(TelegramApiResponse response) {
        if (response == null || !response.ok()) {
            var description = response == null ? "empty response" : response.description();
            throw new IllegalStateException("Telegram sendMessage failed: " + description);
        }
    }
}
