package com.payguard.shared.fraud;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

@Builder(toBuilder = true)
public record FraudScoreResponse(
        UUID transactionId,
        double fraudScore,
        @NonNull
        FraudDecision decision,
        String contributingFactor,
        String modelVersion,
        int latencyMs,
        boolean fallbackUsed,
        TransactionFeatures features
) {
}
