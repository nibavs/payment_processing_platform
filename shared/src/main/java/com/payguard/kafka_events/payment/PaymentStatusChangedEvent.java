package com.payguard.kafka_events.payment;

import com.payguard.shared.fraud.FraudDecision;
import com.payguard.shared.payment.TransactionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentStatusChangedEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        String source,
        PaymentStatusChangedData data

) {
    @Builder
    public record PaymentStatusChangedData(
            UUID transactionId,
            UUID merchantId,
            BigDecimal amount,
            String currency,
            String customerEmail,
            TransactionStatus status,
            String yooKassaPaymentId,
            Double fraudScore,
            FraudDecision fraudDecision,
            String failureReason
    ) {}
}
