package com.databuff.apm.web.admin.settings;

import org.springframework.stereotype.Service;

@Service
public class SessionIdleSettingsService {

    /** Default idle logout: 24 hours (seconds). */
    public static final int DEFAULT_IDLE_SECONDS = 86_400;

    private volatile int idleSeconds = DEFAULT_IDLE_SECONDS;

    public int idleSeconds() {
        return idleSeconds;
    }

    public void updateIdleSeconds(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("idle timeout must be positive");
        }
        idleSeconds = seconds;
    }
}
