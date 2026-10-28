package com.payguard.fraud.entity;

import com.payguard.shared.fraud.FraudDecision;
import com.payguard.shared.fraud.TransactionFeatures;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "fraud_score", nullable = false, precision = 4)
    private Double fraudScore;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FraudDecision decision;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features_json", nullable = false, columnDefinition = "jsonb")
    private TransactionFeatures featuresJson;

//    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contributing_factors", nullable = false)
    private String contributingFactor;

    @Column(name = "latency_ms", nullable = false)
    private Integer latencyMs;

    @Column(name = "fallback_used", nullable = false)
    private Boolean fallbackUsed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
