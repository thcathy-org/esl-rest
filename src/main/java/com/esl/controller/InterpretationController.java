package com.esl.controller;

import com.esl.service.InterpretationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/interpretation")
public class InterpretationController {
    private static final Logger log = LoggerFactory.getLogger(InterpretationController.class);
    private static final MediaType TEXT_PLAIN_UTF8 = MediaType.parseMediaType("text/plain;charset=UTF-8");

    private final InterpretationService interpretationService;

    public InterpretationController(InterpretationService interpretationService) {
        this.interpretationService = interpretationService;
    }

    @GetMapping(produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> interpret(@RequestParam String text, @RequestParam String lang) {
        try {
            var result = interpretationService.interpret(text, lang);
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                    .contentType(TEXT_PLAIN_UTF8)
                    .body(result);
        } catch (IllegalArgumentException e) {
            log.warn("Bad interpretation request: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(TEXT_PLAIN_UTF8).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error in interpretation lang={} textLen={}", lang, text == null ? -1 : text.length(), e);
            return ResponseEntity.internalServerError().contentType(TEXT_PLAIN_UTF8).body("Error retrieving interpretation");
        }
    }
}
