package com.payguard.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@Slf4j
class YooKassaWebhookController {
    @PostMapping("/yooKassa")
    public ResponseEntity<String> handleYooKassaWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // TODO: написать логику
            log.info("Received YooKassa webhook: {}", payload);

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.ok("Webhook received");
        }
    }
}
