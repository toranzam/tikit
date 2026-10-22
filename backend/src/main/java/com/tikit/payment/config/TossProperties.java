package com.tikit.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "toss")
public record TossProperties(
        String baseUrl,
        String clientKey,
        String secretKey,
        Duration connectTimeout,
        Duration readTimeout
) {
}
