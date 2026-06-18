package com.databuff.apm.web.auth;

import com.databuff.apm.web.config.JwtProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public final class JwtTokenService {

    private static final String HMAC_ALG = "HmacSHA256";

    private final JwtProperties properties;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
    }

    public String issueToken(String username) {
        return issueToken(username, properties.expirationSeconds());
    }

    public String issueToken(String username, long expirationSeconds) {
        long expiresAt = Instant.now().getEpochSecond() + expirationSeconds;
        String payload = username + "|" + expiresAt;
        String signature = sign(payload);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + signature;
    }

    public Optional<String> parseUsername(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String raw = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
        int dot = raw.lastIndexOf('.');
        if (dot <= 0) {
            return Optional.empty();
        }
        String payload = new String(Base64.getUrlDecoder().decode(raw.substring(0, dot)), StandardCharsets.UTF_8);
        String signature = raw.substring(dot + 1);
        if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        int sep = payload.lastIndexOf('|');
        if (sep <= 0) {
            return Optional.empty();
        }
        long expiresAt = Long.parseLong(payload.substring(sep + 1));
        if (Instant.now().getEpochSecond() > expiresAt) {
            return Optional.empty();
        }
        return Optional.of(payload.substring(0, sep));
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("JWT sign failed", e);
        }
    }
}
