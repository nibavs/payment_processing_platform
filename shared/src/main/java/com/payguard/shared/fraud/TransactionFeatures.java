package com.payguard.shared.fraud;

public record TransactionFeatures(
        long amountCents,
        int hourOfDay,
        int dayOfWeek,
        String merchantCategory,
        long userAvgAmountCents,
        double amountDeviation,
        int txVelocity1h,
        int txVelocity24h,
        boolean newMerchant,
        boolean countryMismatch,
        int failedAttempts24h
) {
}
