package com.payguard.payment.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
public record TransactionResponse(
//        UUID id,
//        UUID merchantId,
        long amount,
//        String currency,
//        String status,
//        BigDecimal fraudScore,
//        String fraudDecision,
        String customerEmail
//        String description,
//        Map<String, Object>metadata,
//        String failureReason,
//        LocalDateTime createdAt
) {
}
