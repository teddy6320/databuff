package com.databuff.apm.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceKeyUtilTest {

    @Test
    void deterministicSlice() {
        String k1 = ServiceKeyUtil.of("order-service");
        String k2 = ServiceKeyUtil.of("order-service");
        assertThat(k1).hasSize(16).isEqualTo(k2);
    }
}
