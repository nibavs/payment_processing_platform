package com.payguard.payment.client;

import com.nibavs.payguard.proto.FraudDecision;
import com.nibavs.payguard.proto.FraudScoreProtoRequest;
import com.nibavs.payguard.proto.FraudScoreProtoResponse;
import com.nibavs.payguard.proto.FraudServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class FraudClient {

    private final FraudServiceGrpc.FraudServiceBlockingStub fraudCheckStub;
    @Value("${services.fraud.fallback.max-amount-rub}")
    private BigDecimal fallbackMaxAmount;


    public FraudScoreProtoResponse score(FraudScoreProtoRequest request) {
        try {
            return fraudCheckStub
                    .withDeadlineAfter(2, TimeUnit.SECONDS)
                    .scoreTransaction(request);
        } catch (Exception e) {
            log.error("Unexpected error calling Fraud Engine for transaction {}: {}. Using fallback.",
                    request.getTransactionId(), e.getMessage());
            return fallbackScore(request.getTransactionId(), new BigDecimal(request.getAmount()));
        }
    }

    private FraudScoreProtoResponse fallbackScore(String transactionId, BigDecimal amount) {
        boolean isSmallAmount = amount.compareTo(fallbackMaxAmount) < 1;

        return FraudScoreProtoResponse.newBuilder()
                .setTransactionId(transactionId)
                .setFraudScore(isSmallAmount ? 0.1 : 0.8)
                .setDecision(isSmallAmount ? FraudDecision.APPROVE : FraudDecision.BLOCK)
                .setContributingFactor(isSmallAmount ? "fallback_small_amount" : "fallback_large_amount")
                .setModelVersion("fallback-v1.0.0")
                .setLatencyMs(0)
                .setFallbackUsed(true)
                .build();
    }
}
