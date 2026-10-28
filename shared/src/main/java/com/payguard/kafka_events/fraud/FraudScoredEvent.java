package com.payguard.kafka_events.fraud;

import com.payguard.shared.fraud.FraudDecision;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record FraudScoredEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        String source,
        FraudScoredData data
) {
    @Builder
    public record FraudScoredData(
            String transactionId,
            String merchantId,
            Double fraudScore,
            FraudDecision fraudDecision,
            String modelVersion,
            long latencyMs,
            boolean fallbackUsed
    ) {
    }
}
