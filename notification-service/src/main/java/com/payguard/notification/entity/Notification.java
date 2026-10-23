package com.payguard.notification.entity;


import com.payguard.shared.notification.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private String recipient;

    @Column(length = 500)
    private String subject;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
