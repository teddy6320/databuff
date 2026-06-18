package com.databuff.apm.common.cluster.coordination;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClusterAdvertiseEndpointTest {

    @Test
    void usesPodIpWhenPresent() {
        assertThat(ClusterAdvertiseEndpoint.resolve("10.0.0.5", 18112)).isEqualTo("10.0.0.5:18112");
    }

    @Test
    void rejectsInvalidPort() {
        assertThatThrownBy(() -> ClusterAdvertiseEndpoint.resolve("10.0.0.5", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
