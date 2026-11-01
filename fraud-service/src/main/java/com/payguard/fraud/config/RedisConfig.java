package com.payguard.fraud.config;

import com.payguard.shared.fraud.FraudScoreResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(GenericJacksonJsonRedisSerializer.builder().build());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(GenericJacksonJsonRedisSerializer.builder().build());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, FraudScoreResponse> redisTemplateFraudScore(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, FraudScoreResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        var valueSerializer =
                new JacksonJsonRedisSerializer<>(FraudScoreResponse.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }
}