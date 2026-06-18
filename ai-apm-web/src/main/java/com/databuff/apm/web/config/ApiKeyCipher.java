package com.databuff.apm.web.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Dev-phase encoding for persisted {@code api_key_cipher} column (not production KMS). */
public final class ApiKeyCipher {

    private ApiKeyCipher() {
    }

    public static String encode(String plain) {
        if (plain == null || plain.isBlank()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String cipher) {
        if (cipher == null || cipher.isBlank()) {
            return null;
        }
        return new String(Base64.getDecoder().decode(cipher), StandardCharsets.UTF_8);
    }
}
