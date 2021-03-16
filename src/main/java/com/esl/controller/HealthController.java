package com.esl.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @RequestMapping(value = "/health")
    public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("OK");
	}

}
