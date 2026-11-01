package com.payguard.notification.consumer;


import com.payguard.kafka_events.fraud.FraudAlertHighEvent;
import com.payguard.kafka_events.user.UserRegisteredEvent;
import com.payguard.notification.service.NotificationProcessor;
import com.payguard.shared.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class FraudAlertEventConsumer {

    private final NotificationProcessor notificationProcessor;

    @KafkaListener(topics = {"fraud.alert.high"}, groupId = "notification-service")
    public void handleFraudAlertHigh(FraudAlertHighEvent event) {
        try {
            log.info("Received fraud.alert.high event: {}", event.eventId());

            FraudAlertHighEvent.FraudAlertHighData data = event.data();
            if (data == null) return;


            String subject = "PayGuard ALERT — Suspicious Transaction Blocked";
            String body = String.format(
                    "FRAUD ALERT\n\n"
                            + "A suspicious transaction has been blocked.\n\n"
                            + "Transaction ID: %s\n"
                            + "Amount: $%.2f\n"
                            + "Fraud Score: %.1f%%\n"
                            + "Decision: BLOCKED\n\n"
                            + "— PayGuard Security Team",
                    data.transactionId(), data.amount(), data.fraudScore() * 100
            );

            notificationProcessor.processNotification(UUID.fromString(data.merchantId()),
                    NotificationChannel.EMAIL, "admin@payguard.com", "",
                    "FRAUD_ALERT", subject, body
            );
        } catch (Exception e) {
            log.error("Failed to process fraud.alert.high event: {}", e.getMessage(), e);
        }
    }
}
