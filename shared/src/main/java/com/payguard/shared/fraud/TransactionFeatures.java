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
    public com.nibavs.payguard.proto.TransactionFeatures toProtoFeatures() {
        return com.nibavs.payguard.proto.TransactionFeatures.newBuilder()
                .setAmount(amount.toString())
                .setHourOfDay(hourOfDay)
                .setDayOfWeek(dayOfWeek)
                .setMerchantCategory(merchantCategory)
                .setUserAvgAmountCents(userAvgAmountCents)
                .setAmountDeviation(amountDeviation.toString())
                .setTxVelocity1H(txVelocity1h)
                .setTxVelocity24H(txVelocity24h)
                .setNewMerchant(newMerchant)
                .setCountryMismatch(countryMismatch)
                .setFailedAttempts24H(failedAttempts24h)
                .build();
    }
}
