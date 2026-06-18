package com.databuff.apm.web.config;

import com.databuff.apm.web.auth.AuthInterceptor;
import com.databuff.apm.web.auth.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Bean
    JwtTokenService jwtTokenService(JwtProperties jwtProperties) {
        return new JwtTokenService(jwtProperties);
    }

    @Bean
    AuthInterceptor authInterceptor(JwtTokenService jwtTokenService) {
        return new AuthInterceptor(jwtTokenService);
    }
}
