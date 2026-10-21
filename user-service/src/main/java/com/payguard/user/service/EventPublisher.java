package com.payguard.user.service;

import com.payguard.user.config.KafkaConfig;
import com.payguard.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.payguard.kafka_events.user.UserRegisteredEvent;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;


    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = createEvent(user);

        kafkaTemplate.send(
                KafkaConfig.USER_REGISTERED_TOPIC,
                user.getId().toString(),
                event
        );

        log.info("Published user.registered event for user: {}", user.getId());
    }

    private UserRegisteredEvent createEvent(User user) {
        return UserRegisteredEvent.builder()
                .eventId("evt_" + UUID.randomUUID().toString().substring(0, 8))
                .eventType("user.registered")
                .timestamp(LocalDateTime.now())
                .source("user-service")
                .data(UserRegisteredEvent.UserRegisteredData.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .merchantName(user.getMerchantName())
                        .merchantCategory(user.getMerchantCategory())
                        .country(user.getCountry())
                        .build())
                .build();
    }

}
