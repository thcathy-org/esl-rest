package com.esl.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);

    private final JavaMailSender mailSender;
    private final String recipient;

    public FeedbackController(JavaMailSender mailSender,
                              @Value("${feedback.recipient}") String recipient) {
        this.mailSender = mailSender;
        this.recipient = recipient;
    }

    public record FeedbackRequest(
        String channel,
        String contact,
        String email,
        String message
    ) {}

    @PostMapping
    public ResponseEntity<Void> submit(@RequestBody FeedbackRequest request) {
        if (request == null || StringUtils.isBlank(request.message())) {
            return ResponseEntity.badRequest().build();
        }
        var channel = flatten(request.channel());
        var contact = flatten(request.contact());
        if (contact == null) {
            contact = flatten(request.email());
            if (contact != null && channel == null) {
                channel = "email";
            }
        }
        var message = request.message().trim();
        var loggedChannel = channel == null ? "none" : channel;
        var loggedContact = contact == null ? "none" : contact;
        try {
            var mail = new SimpleMailMessage();
            mail.setTo(recipient);
            mail.setSubject(subject(channel, contact));
            mail.setText(body(channel, contact, message));
            mailSender.send(mail);
            log.info("Feedback email sent via {} contact {}", loggedChannel, loggedContact);
        } catch (Exception e) {
            log.error("Failed to send feedback email via {} contact {}", loggedChannel, loggedContact, e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

    private static String flatten(String value) {
        if (value == null) {
            return null;
        }
        return StringUtils.trimToNull(value.replaceAll("[\\r\\n]+", " "));
    }

    private static String subject(String channel, String contact) {
        if ("email".equals(channel) && contact != null) {
            return "FunFunSpell Feedback from " + contact;
        }
        if (channel != null) {
            return "FunFunSpell Feedback via " + channel;
        }
        return "FunFunSpell Feedback";
    }

    private static String body(String channel, String contact, String message) {
        if (channel == null && contact == null) {
            return "No contact given.\n\n" + message;
        }
        var text = new StringBuilder();
        if (channel != null) {
            text.append("Channel: ").append(channel).append('\n');
        }
        if (contact != null) {
            text.append("Contact: ").append(contact).append('\n');
        }
        text.append('\n').append(message);
        return text.toString();
    }
}
