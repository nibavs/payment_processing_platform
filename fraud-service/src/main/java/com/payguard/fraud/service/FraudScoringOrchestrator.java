package com.payguard.fraud.service;

import com.payguard.fraud.config.KafkaConfig;
import com.payguard.fraud.entity.FraudAuditLog;
import com.payguard.fraud.repository.AuditLogRepository;
import com.payguard.kafka_events.fraud.FraudAlertHighEvent;
import com.payguard.kafka_events.fraud.FraudScoredEvent;
import com.payguard.shared.fraud.FraudDecision;
import com.payguard.shared.fraud.FraudScoreRequest;
import com.payguard.shared.fraud.FraudScoreResponse;
import com.payguard.shared.fraud.TransactionFeatures;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudScoringOrchestrator {

    private final RuleBasedScoringService ruleBasedScoringService;
    private final ScoreCachingService scoreCachingService;
    private final VelocityTrackingService velocityTrackingService;
    private final AuditLogRepository auditLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Score a transaction end-to-end:
     * 1. Check cache
     * 2. Gather velocity features
     * 3. Run scoring
     * 4. Cache result
     * 5. Update velocity counters
     * 6. Save audit log
     * 7. Publish Kafka events
     */
    public FraudScoreResponse scoreTransaction(FraudScoreRequest transaction) {
        String transactionId = transaction.transactionId().toString();
        String merchantId = transaction.merchantId().toString();
        BigDecimal amountCents = transaction.amount();
        String customerEmail = transaction.customerEmail();
        String merchantCategory = transaction.merchantCategory();
        long timestamp = transaction.timestamp();

        long startTime = System.currentTimeMillis();

        // Step 1: Check cache
        Optional<FraudScoreResponse> cached = scoreCachingService.get(merchantId, amountCents, customerEmail);
        if (cached.isPresent()) {
            log.info("Cache hit for transaction {}", transactionId);
            FraudScoreResponse cachedResult = cached.get();
            cachedResult.toBuilder()
                    .transactionId(UUID.fromString(transactionId))
                    .build();
            return cachedResult;
        }

//         Step 2: Gather velocity features from Redis
        int velocity1h = velocityTrackingService.getCount(customerEmail, "1h");
        int velocity24h = velocityTrackingService.getCount(customerEmail, "24h");
        int failedAttempts = velocityTrackingService.getFailedAttempts(customerEmail);

        // Calculate time features
        Instant instant = Instant.ofEpochSecond(timestamp);
        ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC")); // TODO: User country convert to ZoneId
        int hourOfDay = zdt.getHour();
        int dayOfWeek = zdt.getDayOfWeek().getValue();

        // Calculate amount deviation
        long avgAmount = 5000L; // TODO: Pull from historical data or user profile
        BigDecimal deviation = amountCents.divide(BigDecimal.valueOf(avgAmount), RoundingMode.HALF_DOWN);

        // Step 3: Assemble features
        TransactionFeatures features = TransactionFeatures.builder()
                .amount(amountCents)
                .hourOfDay(hourOfDay)
                .dayOfWeek(dayOfWeek)
                .merchantCategory(merchantCategory != null ? merchantCategory : "unknown")
                .userAvgAmountCents(avgAmount)
                .amountDeviation(deviation)
                .txVelocity1h(velocity1h)
                .txVelocity24h(velocity24h)
                .newMerchant(false) // TODO: determine from transaction history
                .countryMismatch(false) // TODO: compare card country vs merchant country
                .failedAttempts24h(failedAttempts)
                .build();

        // Step 4: Run scoring
        FraudScoreResponse result = ruleBasedScoringService.score(transactionId, features);

        // Update latency to include full orchestration time
        int totalLatency = (int) (System.currentTimeMillis() - startTime);
        result = result.toBuilder().latencyMs(totalLatency).build();

        // Step 5: Cache the result
        scoreCachingService.put(merchantId, amountCents, customerEmail, result);

        // Step 6: Update velocity counters
        velocityTrackingService.incrementAndGet(customerEmail, "1h", Duration.ofHours(1));
        velocityTrackingService.incrementAndGet(customerEmail, "24h", Duration.ofHours(24));
        if (!result.decision().equals(FraudDecision.APPROVE)) {
            velocityTrackingService.recordFailedAttempt(customerEmail);
        }

        // Step 7: Save audit log
        saveAuditLog(transactionId, merchantId, result);

        // Step 8: Publish Kafka events
        publishEvents(transactionId, merchantId, result);

        log.info("Scored transaction {} → {} (score={}, latency={}ms)",
                transactionId, result.decision(), result.fraudScore(), totalLatency);

        return result;
    }

    private void publishEvents(String transactionId, String merchantId, FraudScoreResponse result) {
        try {
            FraudScoredEvent fraudScoredEvent = FraudScoredEvent.builder()
                    .eventId("evt_" + UUID.randomUUID().toString().substring(0, 8))
                    .eventType(KafkaConfig.FRAUD_SCORED_TOPIC)
                    .timestamp(LocalDateTime.now())
                    .source("fraud-service")
                    .data(FraudScoredEvent.FraudScoredData.builder()
                            .merchantId(merchantId)
                            .fraudScore(result.fraudScore())
                            .fraudDecision(result.decision())
                            .modelVersion(result.modelVersion())
                            .latencyMs(result.latencyMs())
                            .fallbackUsed(result.fallbackUsed())
                            .build())
                    .build();

            kafkaTemplate.send(KafkaConfig.FRAUD_SCORED_TOPIC, transactionId, fraudScoredEvent);

            if (result.decision().equals(FraudDecision.BLOCK)) {
                FraudAlertHighEvent fraudAlertHighEvent = FraudAlertHighEvent.builder()
                        .eventId("evt_" + UUID.randomUUID().toString().substring(0, 8))
                        .eventType(KafkaConfig.FRAUD_SCORED_TOPIC)
                        .timestamp(LocalDateTime.now())
                        .source("fraud-service")
                        .data(FraudAlertHighEvent.FraudAlertHighData.builder()
                                .transactionId(transactionId)
                                .merchantId(merchantId)
                                .fraudScore(result.fraudScore())
                                .fraudDecision(result.decision())
                                .amount(result.features().amount())
                                .contributingFactor(result.contributingFactor())
                                .build())
                        .build();

                kafkaTemplate.send(KafkaConfig.FRAUD_ALERT_HIGH_TOPIC, transactionId, fraudAlertHighEvent);
            }
        } catch (Exception e) {
            log.error("Failed to publish fraud event for transaction {}: {}", transactionId, e.getMessage());
        }
    }

    private void saveAuditLog(String transactionId, String merchantId, FraudScoreResponse result) {
        try {
            FraudAuditLog auditLog = FraudAuditLog.builder()
                    .transactionId(UUID.fromString(transactionId))
                    .merchantId(UUID.fromString(merchantId))
                    .fraudScore(result.fraudScore())
                    .decision(result.decision())
                    .modelVersion(result.modelVersion())
                    .featuresJson(result.features())
                    .contributingFactor(result.contributingFactor())
                    .latencyMs(result.latencyMs())
                    .fallbackUsed(result.fallbackUsed())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log for transaction {}: {}", transactionId, e.getMessage());
        }
    }
}