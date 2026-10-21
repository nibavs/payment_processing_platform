package com.payguard.payment.dto;

import com.payguard.shared.fraud.FraudDecision;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Builder
public record TransactionResponse(
        UUID id,
        UUID merchantId,
        BigDecimal amount,
        String currency,
        String status,
        Double fraudScore,
        FraudDecision fraudDecision,
        String customerEmail,
        String paymentUrl,
        String description,
        Map<String, Object> metadata,
        String failureReason,
        LocalDateTime createdAt
) {
}
