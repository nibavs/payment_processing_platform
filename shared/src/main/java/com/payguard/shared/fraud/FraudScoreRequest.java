package com.payguard.shared.fraud;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record FraudScoreRequest(
        UUID transactionId,
        UUID merchantId,
        String currency,
        String customerEmail,
        BigDecimal amount
) {
}
