package com.esl.service.rest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelegramNotificationServiceTest {

    @Test
    void isConfigured_returnsFalseWhenTokenMissing() {
        var service = new TelegramNotificationService("", "123", 10);
        assertFalse(service.isConfigured());
    }

    @Test
    void isConfigured_returnsFalseWhenChatIdMissing() {
        var service = new TelegramNotificationService("token", "", 10);
        assertFalse(service.isConfigured());
    }

    @Test
    void isConfigured_returnsTrueWhenBothSet() {
        var service = new TelegramNotificationService("token", "123", 10);
        assertTrue(service.isConfigured());
    }

    @Test
    void sendMessage_throwsWhenNotConfigured() {
        var service = new TelegramNotificationService("", "", 10);
        var ex = assertThrows(IllegalStateException.class, () -> service.sendMessage("hello"));
        assertTrue(ex.getMessage().contains("not configured"));
    }

    @Test
    void requireOkResponse_acceptsSuccessfulResponse() {
        assertDoesNotThrow(() ->
                TelegramNotificationService.requireOkResponse(new TelegramNotificationService.TelegramApiResponse(true, null)));
    }

    @Test
    void requireOkResponse_rejectsFailedResponse() {
        var ex = assertThrows(IllegalStateException.class, () ->
                TelegramNotificationService.requireOkResponse(
                        new TelegramNotificationService.TelegramApiResponse(false, "Bad Request: chat not found")));
        assertTrue(ex.getMessage().contains("chat not found"));
    }

    @Test
    void requireOkResponse_rejectsNullResponse() {
        var ex = assertThrows(IllegalStateException.class, () ->
                TelegramNotificationService.requireOkResponse(null));
        assertTrue(ex.getMessage().contains("empty response"));
    }
}
