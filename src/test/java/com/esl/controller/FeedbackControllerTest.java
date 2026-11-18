package com.esl.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FeedbackControllerTest {

    private static final String RECIPIENT = "contactfunfunspell@gmail.com";

    @Test
    void submit_emailChannel_showsChannelAndContact() {
        var sender = mock(JavaMailSender.class);
        var controller = new FeedbackController(sender, RECIPIENT);

        var response = controller.submit(new FeedbackController.FeedbackRequest(
                "email", " parent@email.com ", "parent@email.com", " The audio was quiet. "));

        assertEquals(200, response.getStatusCode().value());
        var mail = sentMail(sender);
        assertEquals(RECIPIENT, mail.getTo()[0]);
        assertEquals(1, mail.getTo().length);
        assertEquals("FunFunSpell Feedback from parent@email.com", mail.getSubject());
        assertTrue(mail.getText().contains("Channel: email"));
        assertTrue(mail.getText().contains("Contact: parent@email.com"));
        assertTrue(mail.getText().contains("The audio was quiet."));
        verify(sender, times(1)).send(mail);
    }

    @Test
    void submit_whatsapp_namesTheChannelAndContact() {
        var sender = mock(JavaMailSender.class);
        var controller = new FeedbackController(sender, RECIPIENT);

        var response = controller.submit(new FeedbackController.FeedbackRequest(
                "WhatsApp", "+852 9123 4567", null, "Please check lesson 3."));

        assertEquals(200, response.getStatusCode().value());
        var mail = sentMail(sender);
        assertEquals(RECIPIENT, mail.getTo()[0]);
        assertEquals("FunFunSpell Feedback via whatsapp", mail.getSubject());
        assertTrue(mail.getText().contains("Channel: whatsapp"));
        assertTrue(mail.getText().contains("Contact: +852 9123 4567"));
        assertTrue(mail.getText().contains("Please check lesson 3."));
        verify(sender, times(1)).send(mail);
    }

    @Test
    void submit_wechatAndMessenger_includeTheContactInTheBody() {
        var sender = mock(JavaMailSender.class);
        var controller = new FeedbackController(sender, RECIPIENT);

        controller.submit(new FeedbackController.FeedbackRequest(
                "wechat", "wxid_learner", null, "Hi"));
        var wechat = sentMail(sender);
        assertTrue(wechat.getSubject().contains("wechat"));
        assertTrue(wechat.getText().contains("Channel: wechat"));
        assertTrue(wechat.getText().contains("Contact: wxid_learner"));

        controller.submit(new FeedbackController.FeedbackRequest(
                "messenger", "https://m.me/jane", null, "Hi"));
        var messenger = sentMails(sender).get(1);
        assertTrue(messenger.getSubject().contains("messenger"));
        assertTrue(messenger.getText().contains("Channel: messenger"));
        assertTrue(messenger.getText().contains("Contact: https://m.me/jane"));
    }

    @Test
    void submit_legacyEmailOnlyPayload_stillSendsOneFeedbackEmail() {
        var sender = mock(JavaMailSender.class);
        var controller = new FeedbackController(sender, RECIPIENT);

        var response = controller.submit(new FeedbackController.FeedbackRequest(
                null, null, "learner@example.com", "Still works"));

        assertEquals(200, response.getStatusCode().value());
        var mail = sentMail(sender);
        assertTrue(mail.getText().contains("Channel: email"));
        assertTrue(mail.getText().contains("Contact: learner@example.com"));
        verify(sender, times(1)).send(mail);
    }

    @Test
    void submit_rejectsMissingContactBadEmailAndUnknownChannel() {
        var sender = mock(JavaMailSender.class);
        var controller = new FeedbackController(sender, RECIPIENT);

        assertEquals(400, controller.submit(new FeedbackController.FeedbackRequest(
                "whatsapp", "  ", null, "Hello")).getStatusCode().value());
        assertEquals(400, controller.submit(new FeedbackController.FeedbackRequest(
                "email", "not-an-email", null, "Hello")).getStatusCode().value());
        assertEquals(400, controller.submit(new FeedbackController.FeedbackRequest(
                "email", "parent@email.com,attacker@evil.com", null, "Hello")).getStatusCode().value());
        assertEquals(400, controller.submit(new FeedbackController.FeedbackRequest(
                "line", "learner", null, "Hello")).getStatusCode().value());
        assertEquals(400, controller.submit(new FeedbackController.FeedbackRequest(
                "messenger", "Jane", null, "   ")).getStatusCode().value());
        assertEquals(400, controller.submit(new FeedbackController.FeedbackRequest(
                null, "+85291234567", null, "Hello")).getStatusCode().value());
        verify(sender, times(0)).send(org.mockito.ArgumentMatchers.<SimpleMailMessage>any());
    }

    @Test
    void submit_keepsTheContactOnOneLine() {
        var sender = mock(JavaMailSender.class);
        var controller = new FeedbackController(sender, RECIPIENT);

        assertEquals(400, controller.submit(new FeedbackController.FeedbackRequest(
                "email", "parent@email.com\r\nBcc: evil@example.com", null, "Hello")).getStatusCode().value());

        var response = controller.submit(new FeedbackController.FeedbackRequest(
                "whatsapp", "+852 9123 4567\r\nBcc: evil@example.com", null, "Hello"));

        assertEquals(200, response.getStatusCode().value());
        var mail = sentMail(sender);
        assertFalse(mail.getText().contains("\r"));
        assertFalse(mail.getText().contains("\nBcc"));
        assertTrue(mail.getText().contains("Contact: +852 9123 4567 Bcc: evil@example.com"));
        verify(sender, times(1)).send(mail);
    }

    @Test
    void submit_returnsServerErrorWhenMailFailsAndDoesNotSendAnotherMessage() {
        var sender = mock(JavaMailSender.class);
        doThrow(new RuntimeException("smtp down")).when(sender).send(org.mockito.ArgumentMatchers.<SimpleMailMessage>any());
        var controller = new FeedbackController(sender, RECIPIENT);

        var response = controller.submit(new FeedbackController.FeedbackRequest(
                "wechat", "wxid_learner", null, "Hello"));

        assertEquals(500, response.getStatusCode().value());
        verify(sender, times(1)).send(org.mockito.ArgumentMatchers.<SimpleMailMessage>any());
    }

    private static SimpleMailMessage sentMail(JavaMailSender sender) {
        return sentMails(sender).get(0);
    }

    private static List<SimpleMailMessage> sentMails(JavaMailSender sender) {
        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender, atLeastOnce()).send(captor.capture());
        return captor.getAllValues();
    }
}
