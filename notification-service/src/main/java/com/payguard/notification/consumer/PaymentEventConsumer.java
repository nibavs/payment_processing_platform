package com.payguard.notification.consumer;

import com.payguard.kafka_events.payment.PaymentStatusChangedEvent;
import com.payguard.notification.service.NotificationProcessor;
import com.payguard.shared.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationProcessor notificationProcessor;

    @KafkaListener(topics = "payment.completed", groupId = "notification-service")
    public void handlePaymentCompleted(PaymentStatusChangedEvent event) {
        try {
            log.info("Received payment.completed event: {}", event.eventId());

            PaymentStatusChangedEvent.PaymentStatusChangedData data = event.data();

            String subject = "PayGuard — Payment Receipt";
            String body = String.format(
                    """
                            Hello,
                            
                            Your payment has been processed successfully.
                            
                            --- Payment Receipt ---
                            Transaction ID: %s
                            Amount: %s %.2f
                            Status: COMPLETED
                            -----------------------
                            
                            Thank you for using PayGuard.
                            
                            — PayGuard""",
                    data.transactionId(), data.currency().toUpperCase(), data.amount()
            );

            notificationProcessor.processNotification(
                    data.merchantId(), NotificationChannel.EMAIL, data.customerEmail(), "",
                    "PAYMENT_RECEIPT", subject, body
            );
        } catch (Exception e) {
            log.error("Failed to process payment.completed: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service")
    public void handlePaymentFailed(PaymentStatusChangedEvent event) {
        try {
            log.info("Received payment.failed event: {}", event.eventId());

            PaymentStatusChangedEvent.PaymentStatusChangedData data = event.data();
            String subject = "PayGuard — Payment Failed";
            String body = String.format(
                    """
                            Hello,
                            
                            Your payment (Transaction: %s) was not processed.
                            Reason: %s
                            
                            Please try again.
                            
                            — PayGuard""",
                    data.transactionId(), data.failureReason()
            );

            notificationProcessor.processNotification(
                    data.merchantId(), NotificationChannel.EMAIL, data.customerEmail(), "",
                    "PAYMENT_FAILED", subject, body
            );
        } catch (Exception e) {
            log.error("Failed to process payment.failed: {}", e.getMessage(), e);
        }
    }
}
