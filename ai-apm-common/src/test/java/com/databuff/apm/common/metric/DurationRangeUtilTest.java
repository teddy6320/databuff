package com.databuff.apm.common.metric;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DurationRangeUtilTest {

    @Test
    void bucketsDurationNanos() {
        assertThat(DurationRangeUtil.bucket(10_000_000L)).isEqualTo("0-50ms");
        assertThat(DurationRangeUtil.bucket(80_000_000L)).isEqualTo("50-100ms");
        assertThat(DurationRangeUtil.bucket(120_000_000L)).isEqualTo("100-200ms");
        assertThat(DurationRangeUtil.bucket(4_000_000_000L)).isEqualTo("3000ms+");
    }
}
