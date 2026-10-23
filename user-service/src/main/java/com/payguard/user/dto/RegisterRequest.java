package com.payguard.user.dto;

import com.payguard.shared.notification.NotificationChannel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email (message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+79[0-9]{9}$",
                message = "Phone number must match +79XXXXXXXXX"
        )
        String phoneNumber,

        @Nullable
        NotificationChannel preferableNotificationChannel,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "Merchant name is required")
        String merchantName,

        @NotBlank(message = "Merchant category is required")
        String merchantCategory,

        @NotBlank(message = "Country is required")
        @Size(min = 3, max = 3, message = "Country must be a 3-letter ISO code")
        String country
) {}
