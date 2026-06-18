package com.databuff.apm.web.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyCipherTest {

    @Test
    void roundTripsApiKey() {
        assertThat(ApiKeyCipher.decode(ApiKeyCipher.encode("sk-secret"))).isEqualTo("sk-secret");
    }

    @Test
    void blankReturnsNull() {
        assertThat(ApiKeyCipher.encode("  ")).isNull();
        assertThat(ApiKeyCipher.decode(null)).isNull();
    }
}
