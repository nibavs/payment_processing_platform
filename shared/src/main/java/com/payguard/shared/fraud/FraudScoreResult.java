package com.payguard.shared.fraud;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record FraudScoreResult(
        UUID transactionId,
        double fraudScore,
        @NonNull
        FraudDecision decision,
        List<String>contributingFactors,
        String modelVersion,
        int latencyMs,
        boolean fallbackUsed,
        Map<String, Object>features
) {
}
