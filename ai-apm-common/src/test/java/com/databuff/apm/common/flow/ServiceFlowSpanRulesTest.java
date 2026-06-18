package com.databuff.apm.common.flow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceFlowSpanRulesTest {

    @Test
    void detectsSqlResources() {
        assertThat(ServiceFlowSpanRules.isComponentResource("INSERT INTO demo_order VALUES (?)")).isTrue();
        assertThat(ServiceFlowSpanRules.isComponentResource("GET /demo/checkout")).isFalse();
    }
}
