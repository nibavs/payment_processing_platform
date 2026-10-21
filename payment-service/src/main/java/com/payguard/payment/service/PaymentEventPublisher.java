package com.payguard.payment.service;

import com.payguard.kafka_events.payment.PaymentStatusChangedEvent;
import com.payguard.payment.config.KafkaConfig;
import com.payguard.payment.entity.Transaction;
import com.payguard.shared.fraud.FraudDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentStatusChangedEvent> kafkaTemplate;

    public void publishPaymentCreated(Transaction transaction) {
        publish(KafkaConfig.PAYMENT_CREATED_TOPIC, "payment.created", transaction, null);
    }

    public void publishPaymentCompleted(Transaction transaction) {
        publish(KafkaConfig.PAYMENT_COMPLETED_TOPIC, "payment.completed", transaction, null);
    }

    public void publishPaymentFailed(Transaction transaction, String failureReason) {
        publish(KafkaConfig.PAYMENT_FAILED_TOPIC, "payment.failed", transaction, failureReason);
    }

    private void publish(String topic, String eventType, Transaction transaction, String failureReason) {
        try {
            Optional<String> yooKassaPaymentId = Optional.ofNullable(transaction.getYooKassaPaymentId());
            Optional<Double> fraudScore = Optional.ofNullable(transaction.getFraudScore());
            Optional<FraudDecision> fraudDecision = Optional.ofNullable(transaction.getFraudDecision());
            Optional<String> failureRes = Optional.ofNullable(failureReason);


            PaymentStatusChangedEvent.PaymentStatusChangedData data = PaymentStatusChangedEvent.PaymentStatusChangedData.builder()
                    .transactionId(transaction.getId())
                    .merchantId(transaction.getMerchantId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .customerEmail(transaction.getCustomerEmail())
                    .status(transaction.getStatus())
                    .yooKassaPaymentId(yooKassaPaymentId.orElse(null))
                    .fraudScore(fraudScore.orElse(null))
                    .fraudDecision(fraudDecision.orElse(null))
                    .failureReason(failureRes.orElse(null))
                    .build();

            PaymentStatusChangedEvent event = PaymentStatusChangedEvent.builder()
                    .eventId(UUID.randomUUID().toString().substring(0, 8))
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .source("payment-service")
                    .data(data)
                    .build();

            kafkaTemplate.send(topic, transaction.getMerchantId().toString(), event);
            log.info("Published {} for transaction {}", eventType, transaction.getId());
        } catch (Exception e) {
            log.error("Failed to publish {} for transaction {}: {}",
                    eventType, transaction.getId(), e);
        }
    }
}