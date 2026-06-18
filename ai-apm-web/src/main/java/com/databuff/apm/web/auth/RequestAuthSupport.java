package com.databuff.apm.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public final class RequestAuthSupport {

    public static final String USERNAME_ATTRIBUTE = "authUsername";

    private RequestAuthSupport() {
    }

    public static String resolveUserName(HttpServletRequest request, String requestUserName, JwtTokenService jwtTokenService) {
        if (requestUserName != null && !requestUserName.isBlank()) {
            return requestUserName.trim();
        }
        Object attribute = request == null ? null : request.getAttribute(USERNAME_ATTRIBUTE);
        if (attribute instanceof String username && !username.isBlank()) {
            return username.trim();
        }
        if (request != null && jwtTokenService != null) {
            return jwtTokenService.parseUsername(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");
        }
        return "";
    }
}
