package com.payguard.notification.consumer;


import com.payguard.kafka_events.user.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    @KafkaListener(topics = {"user.registered"}, groupId = "notification-service")
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("Received user.registered event: {}", event.eventId());

            UserRegisteredEvent.UserRegisteredData data = event.data();
            if (data == null) return;

            String userId = data.userId().toString();
            String email = data.email();
            String merchantName = data.merchantName();

            String subject = "Welcome to PayGuard!";
            String body = String.format(
                    "Hello %s,\n\n"
                            + "Welcome to PayGuard! Your merchant account is ready.\n\n"
                            + "You can now:\n"
                            + "- Process payments via our API\n"
                            + "- Monitor transactions in real-time\n"
                            + "- View fraud scores and analytics\n\n"
                            + "— The PayGuard Team",
                    merchantName
            );

            log.info("Message consumed:\nuserId - {},\nemail - {}\nmerchantName - {}\nsubject - {}\nbody - {}",
                    userId,
                    email,
                    merchantName,
                    subject,
                    body
            );

            // TODO: process notification
        } catch (Exception e) {
            log.error("Failed to process user.registered: {}", e.getMessage(), e);
        }
    }
}
