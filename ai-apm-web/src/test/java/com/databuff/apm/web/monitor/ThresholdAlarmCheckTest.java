package com.databuff.apm.web.monitor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThresholdAlarmCheckTest {

    @Test
    void detectsGreaterThanThreshold() {
        assertThat(ThresholdAlarmCheck.breached(0.06, 0.05, "gt")).isTrue();
        assertThat(ThresholdAlarmCheck.breached(0.04, 0.05, "gt")).isFalse();
    }

    @Test
    void usesGreaterOrEqualForUnknownComparator() {
        assertThat(ThresholdAlarmCheck.breached(0.05, 0.05, "gte")).isTrue();
    }
}
