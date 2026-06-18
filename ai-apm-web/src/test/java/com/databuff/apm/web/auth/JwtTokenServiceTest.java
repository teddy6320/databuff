package com.databuff.apm.web.auth;

import com.databuff.apm.web.config.JwtProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    @Test
    void issuesAndParsesToken() {
        JwtTokenService service = new JwtTokenService(new JwtProperties("secret-key", 3600));
        String token = service.issueToken("admin");
        assertThat(service.parseUsername(token)).contains("admin");
    }

    @Test
    void issuesTokenWithCustomExpiration() {
        JwtTokenService service = new JwtTokenService(new JwtProperties("secret-key", 3600));
        String token = service.issueToken("admin", 86_400);
        assertThat(service.parseUsername(token)).contains("admin");
    }

    @Test
    void rejectsTamperedToken() {
        JwtTokenService service = new JwtTokenService(new JwtProperties("secret-key", 3600));
        String token = service.issueToken("admin") + "x";
        assertThat(service.parseUsername(token)).isEmpty();
    }

    @Test
    void acceptsBearerPrefix() {
        JwtTokenService service = new JwtTokenService(new JwtProperties("secret-key", 3600));
        String token = service.issueToken("operator");
        assertThat(service.parseUsername("Bearer " + token)).contains("operator");
    }

    @Test
    void rejectsBlankAndMalformedTokens() {
        JwtTokenService service = new JwtTokenService(new JwtProperties("secret-key", 3600));
        assertThat(service.parseUsername(null)).isEmpty();
        assertThat(service.parseUsername("   ")).isEmpty();
        assertThat(service.parseUsername("not-a-jwt")).isEmpty();
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        JwtTokenService service = new JwtTokenService(new JwtProperties("secret-key", 3600));
        long expiredAt = Instant.now().getEpochSecond() - 60;
        String payload = "admin|" + expiredAt;
        Method sign = JwtTokenService.class.getDeclaredMethod("sign", String.class);
        sign.setAccessible(true);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String token = encodedPayload + "." + sign.invoke(service, payload);
        assertThat(service.parseUsername(token)).isEmpty();
    }

    @Test
    void rejectsWrongSignature() {
        JwtTokenService issuer = new JwtTokenService(new JwtProperties("secret-a", 3600));
        JwtTokenService verifier = new JwtTokenService(new JwtProperties("secret-b", 3600));
        assertThat(verifier.parseUsername(issuer.issueToken("admin"))).isEmpty();
    }
}
