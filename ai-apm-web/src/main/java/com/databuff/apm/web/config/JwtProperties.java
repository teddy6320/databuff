package com.databuff.apm.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apm.security.jwt")
public record JwtProperties(String secret, long expirationSeconds) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = "change-me-in-production";
        }
        if (expirationSeconds <= 0) {
            expirationSeconds = 3600;
        }
    }
}
