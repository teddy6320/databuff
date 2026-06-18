package com.databuff.apm.web.auth;

import com.databuff.apm.web.admin.settings.SessionIdleSettingsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Sliding session: refresh JWT on each authenticated request so active users
 * are not logged out by absolute token expiry.
 */
@Component
public class TokenRefreshFilter extends OncePerRequestFilter {

    public static final String REFRESHED_TOKEN_HEADER = "X-Refreshed-Token";

    private final JwtTokenService jwtTokenService;
    private final SessionIdleSettingsService sessionIdleSettingsService;

    public TokenRefreshFilter(JwtTokenService jwtTokenService, SessionIdleSettingsService sessionIdleSettingsService) {
        this.jwtTokenService = jwtTokenService;
        this.sessionIdleSettingsService = sessionIdleSettingsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional<String> username = jwtTokenService.parseUsername(request.getHeader(HttpHeaders.AUTHORIZATION));
        filterChain.doFilter(request, response);
        if (username.isPresent() && response.getStatus() < 400) {
            String refreshed = jwtTokenService.issueToken(username.get(), sessionIdleSettingsService.idleSeconds());
            response.setHeader(REFRESHED_TOKEN_HEADER, refreshed);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/webapi/");
    }
}
