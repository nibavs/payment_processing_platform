package com.payguard.shared.fraud;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionFeatures(
        BigDecimal amount,
        int hourOfDay,
        int dayOfWeek,
        String merchantCategory,
        long userAvgAmountCents,
        BigDecimal amountDeviation,
        int txVelocity1h,
        int txVelocity24h,
        boolean newMerchant,
        boolean countryMismatch,
        int failedAttempts24h
) {
}
