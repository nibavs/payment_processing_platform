package com.payguard.payment.client;

import com.payguard.shared.fraud.FraudDecision;
import com.payguard.shared.fraud.FraudScoreRequest;
import com.payguard.shared.fraud.FraudScoreResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class FraudClient {

    @Value("${services.fraud.fallback.max-amount-rub}")
    private BigDecimal fallbackMaxAmount;

    private final RestClient restClient;

    public FraudClient(
            @Qualifier("fraudRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    // TODO: handle service unavailable
    public FraudScoreResponse score(FraudScoreRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/fraud/score")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FraudScoreResponse.class);

        } catch (Exception e) {
            log.error("Unexpected error calling Fraud Engine for transaction {}: {}. Using fallback.",
                    request.transactionId(), e.getMessage());
            return fallbackScore(request.transactionId(), request.amount());
        }
    }

    private FraudScoreResponse fallbackScore(UUID transactionId, BigDecimal amount) {
        boolean isSmallAmount = amount.compareTo(fallbackMaxAmount) < 1;

        return FraudScoreResponse.builder()
                .transactionId(transactionId)
                .fraudScore(isSmallAmount ? 0.1 : 0.8)
                .decision(isSmallAmount ? FraudDecision.APPROVE : FraudDecision.BLOCK)
                .contributingFactor(isSmallAmount ? "fallback_small_amount" : "fallback_large_amount")
                .modelVersion("fallback-v1.0.0")
                .latencyMs(0)
                .fallbackUsed(true)
                .build();
    }
}
