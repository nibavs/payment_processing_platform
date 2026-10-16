package com.payguard.payment.config;

import com.payguard.payment.config.properties.YooKassaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.deelter.yookassa.YooKassa;

@Configuration
@RequiredArgsConstructor
class YooKassaConfiguration {
    private final YooKassaProperties yooKassaProperties;

    @Bean
    public YooKassa yooKassaClient() {
        return YooKassa.builder()
                .shop(yooKassaProperties.getShopId(), yooKassaProperties.getSecretKey())
                .baseUrl(yooKassaProperties.getApiUrl())
                .build();
    }
}
