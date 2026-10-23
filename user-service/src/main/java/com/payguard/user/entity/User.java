package com.payguard.user.entity;

import com.payguard.shared.notification.NotificationChannel;
import com.payguard.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true, length = 255)
    private String phoneNumber;

    @Column(name = "preferable_notification_channel", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationChannel preferableNotificationChannel;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "merchant_name", nullable = false, length = 255)
    private String merchantName;

    @Column(name = "merchant_category", nullable = false, length = 50)
    private String merchantCategory;

    @Column(nullable = false, length = 3)
    private String country;

    @Column(name = "yooKassa_customer_id", unique = true, length = 255)
    private String yooKassaCustomerId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
