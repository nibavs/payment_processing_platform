package com.payguard.fraud.service;

import com.payguard.shared.fraud.FraudScoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScoreCachingService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${fraud.cache.ttl-seconds}")
    private int ttlSeconds;

    public Optional<FraudScoreResponse> get(String merchantId, BigDecimal amountCents, String email) {
        String key = buildKey(merchantId, amountCents, email);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof FraudScoreResponse result) {
                log.debug("Cache HIT for key: {}", key);
                return Optional.of(result);
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public void put(String merchantId, BigDecimal amountCents, String email, FraudScoreResponse result) {
        String key = buildKey(merchantId, amountCents, email);
        try {
            redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(ttlSeconds));
            log.debug("Cached score for key: {}", key);
        } catch (Exception e) {
            log.warn("Redis cache write failed: {}", e.getMessage());
        }
    }

    private String buildKey(String merchantId, BigDecimal amountCents, String email) {
        return "fraud:score:" + merchantId + ":" + amountCents + ":" + email;
    }
}