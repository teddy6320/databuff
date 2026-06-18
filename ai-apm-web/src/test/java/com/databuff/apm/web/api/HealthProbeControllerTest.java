package com.databuff.apm.web.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthProbeControllerTest {

    @Test
    void healthReturnsUp() {
        assertThat(new HealthProbeController().health()).containsEntry("status", "UP");
    }
}
