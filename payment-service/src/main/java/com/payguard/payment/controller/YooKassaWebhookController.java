package com.payguard.payment.controller;

import com.payguard.payment.service.YooKassaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.deelter.yookassa.YooKassa;
import ru.deelter.yookassa.model.Payment;
import ru.deelter.yookassa.notifications.WebhookNotification;

@RestController
@RequestMapping("/api/v1/webhooks")
@Slf4j
class YooKassaWebhookController {
    private final YooKassa yooKassa;
    private final YooKassaService yooKassaService;

    YooKassaWebhookController(YooKassa yooKassa, YooKassaService yooKassaService) {
        this.yooKassa = yooKassa;
        this.yooKassaService = yooKassaService;
    }

    @PostMapping("/yooKassa")
    public ResponseEntity<String> handleYooKassaWebhook(@RequestBody String payload) {
        try {
            WebhookNotification notification = WebhookNotification.parse(payload);
            if (notification.isPaymentEvent()) {
                Payment payment = yooKassa.getPayment(notification.getObjectId());
                yooKassaService.handlePaymentEventFromYooKassa(payment);
            }

            return ResponseEntity.status(HttpStatus.OK).body("Webhook processed");
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body("Webhook received");
        }
    }
}
