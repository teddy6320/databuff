package com.databuff.apm.web.auth;

import com.databuff.apm.web.admin.account.PortalUserManagementService;
import com.databuff.apm.web.admin.settings.SessionIdleSettingsService;
import com.databuff.apm.web.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {

    @Test
    void loginWithSeedUser() {
        AuthService service = service();
        AuthService.LoginResult result = service.login("admin", "Databuff@123");
        assertThat(result.ok()).isTrue();
        assertThat(result.token()).isNotBlank();
    }

    @Test
    void rejectsInvalidPassword() {
        AuthService service = service();
        assertThat(service.login("admin", "wrong").ok()).isFalse();
    }

    @Test
    void rejectsBlankCredentials() {
        AuthService service = service();
        assertThat(service.login("", "x").ok()).isFalse();
        assertThat(service.login("admin", " ").ok()).isFalse();
    }

    @Test
    void portalLoginAcceptsMd5Password() {
        AuthService service = service();
        assertThat(service.portalLogin("admin", "a8a90a13c6be057b1e48e03ae574e138")).isPresent();
    }

    private static AuthService service() {
        return new AuthService(
                new JwtTokenService(new JwtProperties("secret", 3600)),
                new PortalUserManagementService("admin", "Databuff@123"),
                new SessionIdleSettingsService());
    }
}
