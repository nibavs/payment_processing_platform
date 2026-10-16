package com.payguard.payment.dto;

import lombok.Builder;


@Builder
public record TransactionResponse(
//        UUID id,
//        UUID merchantId,
        long amount,
//        String currency,
//        String status,
//        BigDecimal fraudScore,
//        String fraudDecision,
        String customerEmail,
        String paymentUrl
//        String description,
//        Map<String, Object>metadata,
//        String failureReason,
//        LocalDateTime createdAt
) {
}
