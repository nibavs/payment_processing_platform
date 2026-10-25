package com.payguard.user.dto;

import com.payguard.shared.notification.NotificationChannel;
import com.payguard.user.enums.Role;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponse (
        UUID id,
        String email,
        String phoneNumber,
        NotificationChannel preferableNotificationChannel,
        String avatarUrl,
        String merchantName,
        String merchantCategory,
        String country,
        Role role,
        LocalDateTime createdAt
){

}
