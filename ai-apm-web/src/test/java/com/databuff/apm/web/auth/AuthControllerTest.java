package com.databuff.apm.web.auth;

import com.databuff.apm.web.admin.account.PortalUserManagementService;
import com.databuff.apm.web.admin.settings.SessionIdleSettingsService;
import com.databuff.apm.web.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    private final AuthService authService = new AuthService(
            new JwtTokenService(new JwtProperties("s", 3600)),
            new PortalUserManagementService("admin", "Databuff@123"),
            new SessionIdleSettingsService());

    @Test
    void loginReturnsToken() {
        AuthController controller = new AuthController(authService);
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response =
                controller.login(new AuthController.LoginRequest("admin", "Databuff@123"));
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsKey("token");
    }

    @Test
    void loginRejectsInvalidCredentials() {
        AuthController controller = new AuthController(authService);
        org.springframework.http.ResponseEntity<java.util.Map<String, Object>> response =
                controller.login(new AuthController.LoginRequest("admin", "wrong"));
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).containsKey("message");
    }
}
