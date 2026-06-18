package com.databuff.apm.web.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void appliesDefaultsWhenBlank() {
        JwtProperties props = new JwtProperties("  ", 0);
        assertThat(props.secret()).isEqualTo("change-me-in-production");
        assertThat(props.expirationSeconds()).isEqualTo(3600);
    }

    @Test
    void keepsProvidedValues() {
        JwtProperties props = new JwtProperties("my-secret", 7200);
        assertThat(props.secret()).isEqualTo("my-secret");
        assertThat(props.expirationSeconds()).isEqualTo(7200);
    }
}
