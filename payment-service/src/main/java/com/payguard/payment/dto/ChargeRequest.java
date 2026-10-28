package com.payguard.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record ChargeRequest(
        @Min(value = 50, message = "Minimum amount is 50")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String currency,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Invalid email format")
        String customerEmail,

        String description,

        Map<String, Object> metadata
) {
}
