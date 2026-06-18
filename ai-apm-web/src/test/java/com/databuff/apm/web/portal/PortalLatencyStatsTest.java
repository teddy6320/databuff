package com.databuff.apm.web.portal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalLatencyStatsTest {

    @Test
    void parsesDurationRangeUpperBound() {
        assertThat(PortalLatencyStats.parseDurationUpperBoundNs("0-50ms")).isEqualTo(50_000_000L);
        assertThat(PortalLatencyStats.parseDurationUpperBoundNs("100ms")).isEqualTo(100_000_000L);
    }
}
