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
        var message = request.message().trim();
        var channel = parseChannel(request.channel());
        var contact = trimToNull(request.contact());
        var email = trimToNull(request.email());
        if (channel == null) {
            if (StringUtils.isNotBlank(request.channel()) || email == null) {
                return ResponseEntity.badRequest().build();
            }
            channel = "email";
            contact = email;
        } else if (contact == null && "email".equals(channel)) {
            contact = email;
        }
        if (contact == null || ("email".equals(channel) && !isEmail(contact))) {
            return ResponseEntity.badRequest().build();
        }
        request = new FeedbackRequest(channel, contact, email, message);
        try {
            var mail = new SimpleMailMessage();
            mail.setTo(recipient);
            mail.setSubject(subject(request));
            mail.setText(body(request));
            mailSender.send(mail);
            log.info("Feedback email sent via {} contact {}", request.channel(), request.contact());
        } catch (Exception e) {
            log.error("Failed to send feedback email via {} contact {}", request.channel(), request.contact(), e);
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

    private static String channelName(String channel) {
        return switch (channel) {
            case "email" -> "Email";
            case "whatsapp" -> "WhatsApp";
            case "wechat" -> "WeChat";
            case "messenger" -> "Messenger";
            default -> channel;
        };
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        return StringUtils.trimToNull(value.replaceAll("[\\r\\n]+", " "));
    }

    private static boolean isEmail(String value) {
        return EMAIL.matcher(value).matches();
    }

    private static String subject(FeedbackRequest request) {
        if ("email".equals(request.channel())) {
            return "FunFunSpell Feedback from " + request.contact();
        }
        return "FunFunSpell Feedback via " + channelName(request.channel());
    }

    private static String body(FeedbackRequest request) {
        return "Channel: " + channelName(request.channel()) + "\nContact: " + request.contact() + "\n\n" + request.message();
    }
}
