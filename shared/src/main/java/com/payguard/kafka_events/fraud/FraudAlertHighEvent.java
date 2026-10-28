package com.payguard.kafka_events.fraud;

import com.payguard.shared.fraud.FraudDecision;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
public record FraudAlertHighEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        String source,
        FraudAlertHighData data
) {
    @Builder
    public record FraudAlertHighData(
            String transactionId,
            String merchantId,
            Double fraudScore,
            FraudDecision fraudDecision,
            BigDecimal amount,
            String contributingFactor
    ) {
    }
}
