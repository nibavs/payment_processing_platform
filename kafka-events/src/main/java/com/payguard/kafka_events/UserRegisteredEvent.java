package com.payguard.kafka_events;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRegisteredEvent(
        String eventId,
        String eventType,
        LocalDateTime timestamp,
        String source,
        UserRegisteredData data

) {
    @Builder
    public record UserRegisteredData(
            UUID userId,
            String email,
            String merchantName,
            String merchantCategory,
            String country
    ) {}
}
