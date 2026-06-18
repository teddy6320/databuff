package com.databuff.apm.web.auth;

import com.databuff.apm.web.admin.settings.SessionIdleSettingsService;
import com.databuff.apm.web.config.JwtProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class TokenRefreshFilterTest {

    @Test
    void refreshesTokenForAuthenticatedWebapiRequest() throws Exception {
        JwtTokenService jwtTokenService = new JwtTokenService(new JwtProperties("secret", 3600));
        SessionIdleSettingsService settings = new SessionIdleSettingsService();
        TokenRefreshFilter filter = new TokenRefreshFilter(jwtTokenService, settings);
        String token = jwtTokenService.issueToken("admin", settings.idleSeconds());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/webapi/user/getUserInfo");
        request.addHeader("Authorization", token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            // no-op
        };

        filter.doFilter(request, response, chain);

        String refreshed = response.getHeader(TokenRefreshFilter.REFRESHED_TOKEN_HEADER);
        assertThat(refreshed).isNotBlank();
        assertThat(jwtTokenService.parseUsername(refreshed)).contains("admin");
    }

    @Test
    void skipsNonWebapiPaths() throws Exception {
        TokenRefreshFilter filter = new TokenRefreshFilter(
                new JwtTokenService(new JwtProperties("secret", 3600)),
                new SessionIdleSettingsService());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/assets/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }
}
