package com.databuff.apm.web.admin.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionIdleSettingsServiceTest {

    @Test
    void defaultsToTwentyFourHours() {
        SessionIdleSettingsService service = new SessionIdleSettingsService();
        assertThat(service.idleSeconds()).isEqualTo(SessionIdleSettingsService.DEFAULT_IDLE_SECONDS);
    }

    @Test
    void updatesIdleSeconds() {
        SessionIdleSettingsService service = new SessionIdleSettingsService();
        service.updateIdleSeconds(3600);
        assertThat(service.idleSeconds()).isEqualTo(3600);
    }

    @Test
    void rejectsInvalidIdleSeconds() {
        SessionIdleSettingsService service = new SessionIdleSettingsService();
        assertThatThrownBy(() -> service.updateIdleSeconds(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
