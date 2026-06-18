package com.databuff.apm.web.auth;

import com.databuff.apm.web.config.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuthInterceptorTest {

    @Test
    void allowsValidBearerToken() throws Exception {
        JwtTokenService jwt = new JwtTokenService(new JwtProperties("secret", 3600));
        AuthInterceptor interceptor = new AuthInterceptor(jwt);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt.issueToken("admin"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    void rejectsMissingToken() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(new JwtTokenService(new JwtProperties("secret", 3600)));
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(new MockHttpServletRequest(), response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void allowsOptionsPreflight() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(new JwtTokenService(new JwtProperties("secret", 3600)));
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/webapi/api/v1/cockpit/config");
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
    }
}
