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

import java.util.Locale;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private static final Logger log = LoggerFactory.getLogger(FeedbackController.class);
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+");

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
        var channel = parseChannel(request.channel());
        if (channel == null && StringUtils.isNotBlank(request.channel())) {
            return ResponseEntity.badRequest().build();
        }
        var contact = flatten(request.contact());
        var email = flatten(request.email());
        if (contact == null && (channel == null || "email".equals(channel))) {
            contact = email;
            if (contact != null) {
                channel = "email";
            }
        }
        if (contact != null && (channel == null || ("email".equals(channel) && !isEmail(contact)))) {
            return ResponseEntity.badRequest().build();
        }
        if (contact == null) {
            channel = null;
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

    private static String parseChannel(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "email" -> "email";
            case "whatsapp" -> "whatsapp";
            case "wechat" -> "wechat";
            case "messenger" -> "messenger";
            default -> null;
        };
    }

    private static String flatten(String value) {
        if (value == null) {
            return null;
        }
        return StringUtils.trimToNull(value.replaceAll("[\\r\\n]+", " "));
    }

    private static boolean isEmail(String value) {
        return EMAIL.matcher(value).matches();
    }

    private static String subject(String channel, String contact) {
        if (contact == null) {
            return "FunFunSpell Feedback";
        }
        if ("email".equals(channel)) {
            return "FunFunSpell Feedback from " + contact;
        }
        return "FunFunSpell Feedback via " + channel;
    }

    private static String body(String channel, String contact, String message) {
        if (contact == null) {
            return "No contact given.\n\n" + message;
        }
        return "Channel: " + channel + "\nContact: " + contact + "\n\n" + message;
    }
}
