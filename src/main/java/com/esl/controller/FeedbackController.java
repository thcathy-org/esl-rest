package com.esl.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

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
        @Email @NotBlank String email,
        @NotBlank @Size(max = 2000) String message
    ) {}

    @PostMapping
    public ResponseEntity<Void> submit(@Valid @RequestBody FeedbackRequest request) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(recipient);
            mail.setReplyTo(request.email());
            mail.setSubject("FunFunSpell Feedback from " + request.email());
            mail.setText("From: " + request.email() + "\n\n" + request.message());
            mailSender.send(mail);
            log.info("Feedback email sent from {}", request.email());
        } catch (Exception e) {
            log.error("Failed to send feedback email from {}", request.email(), e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}
