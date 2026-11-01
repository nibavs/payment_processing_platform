package com.payguard.shared.fraud;

import com.nibavs.payguard.proto.FraudScoreProtoResponse;
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
    public FraudScoreProtoResponse toProtoResponse() {
        return FraudScoreProtoResponse.newBuilder()
                .setTransactionId(transactionId.toString())
                .setFraudScore(fraudScore)
                .setDecision(com.nibavs.payguard.proto.FraudDecision.valueOf(decision.name()))
                .setContributingFactor(contributingFactor)
                .setModelVersion(modelVersion)
                .setLatencyMs(latencyMs)
                .setFallbackUsed(fallbackUsed)
                .setFeatures(features.toProtoFeatures())
                .build();
    }
}
