package com.payguard.notification.service;

import com.payguard.notification.entity.Notification;
import com.payguard.notification.repository.NotificationRepository;
import com.payguard.shared.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessor {

    private final EmailService emailService;
    private final SMSService smsService;

    @Value("${notification.retry.max-attempts}")
    private int maxAttempts;
    private final NotificationRepository notificationRepository;

    public void processNotification(UUID userId, NotificationChannel channel, String email,
                                    String phone, String type,
                                    String subject, String body) {

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .channel(channel)
                .status("PENDING")
                .recipient(email)
                .subject(subject)
                .attempts(0)
                .build();

        notification = notificationRepository.save(notification);

        switch (channel) {
            case EMAIL -> sendEmailWithRetry(notification, body);
            case SMS -> smsService.sendSMS(phone, body);
        }
    }

    private void sendEmailWithRetry(Notification notification, String emailBody) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                notification.setAttempts(attempt);
                emailService.sendEmail(notification.getRecipient(),
                        notification.getSubject(), emailBody);

                notification.setStatus("SENT");
                notification.setSentAt(LocalDateTime.now());
                notification.setErrorMessage(null);
                notificationRepository.save(notification);
                log.info("Notification {} sent (attempt {})", notification.getId(), attempt);
                return;

            } catch (Exception e) {
                notification.setErrorMessage(e.getMessage());
                log.warn("Notification {} attempt {} failed: {}",
                        notification.getId(), attempt, e.getMessage());

                if (attempt == maxAttempts) {
                    notification.setStatus("FAILED");
                    notificationRepository.save(notification);
                    log.error("Notification {} permanently failed after {} attempts",
                            notification.getId(), maxAttempts);
                } else {
                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
