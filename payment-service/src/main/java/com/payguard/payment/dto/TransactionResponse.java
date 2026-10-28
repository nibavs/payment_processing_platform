package com.payguard.payment.dto;

import com.payguard.shared.fraud.FraudDecision;
import com.payguard.shared.payment.TransactionStatus;
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
        TransactionStatus status,
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
