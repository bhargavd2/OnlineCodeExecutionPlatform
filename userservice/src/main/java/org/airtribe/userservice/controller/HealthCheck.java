package org.airtribe.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheck {

    @GetMapping("/ping")
    public ResponseEntity<Object> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message","pong");
        return ResponseEntity.ok(response);
    }
}

