package com.payguard.user.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse userResponse
) {}
