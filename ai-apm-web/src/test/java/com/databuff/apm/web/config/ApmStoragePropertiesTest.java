package com.databuff.apm.web.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApmStoragePropertiesTest {

    @Test
    void appliesDefaultsForBlankValues() {
        ApmStorageProperties properties = new ApmStorageProperties(null, " ", "");
        assertThat(properties.metricDatabase()).isEqualTo("databuff");
        assertThat(properties.traceDatabase()).isEqualTo("databuff");
        assertThat(properties.configDatabase()).isEqualTo("databuff");
    }

    @Test
    void keepsExplicitDatabaseNames() {
        ApmStorageProperties properties = new ApmStorageProperties("m", "t", "c");
        assertThat(properties.metricDatabase()).isEqualTo("m");
        assertThat(properties.traceDatabase()).isEqualTo("t");
        assertThat(properties.configDatabase()).isEqualTo("c");
    }
}
