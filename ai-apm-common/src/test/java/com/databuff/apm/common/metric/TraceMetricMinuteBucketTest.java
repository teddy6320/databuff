package com.databuff.apm.common.metric;

import com.databuff.apm.common.model.OptimizedMetric;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraceMetricMinuteBucketTest {

    @Test
    void floorsEndTimeToMinuteBucket() {
        long endNs = 1_700_000_045_500_000_000L;
        assertThat(TraceMetricMinuteBucket.minuteBucketEpochMsFromEndNanos(endNs))
                .isEqualTo(1_700_000_040_000L);
    }

    @Test
    void partitionKeyIncludesMeasurementTagsAndMinute() {
        OptimizedMetric metric = new OptimizedMetric()
                .withTimestamp(1_700_000_045_000_000_000L)
                .withMeasurement("service")
                .withTagValues("ok", "demo", "demo-id", "inst")
                .withFieldValues(1, 0, 10);
        String key = TraceMetricMinuteBucket.aggregationPartitionKey(metric);
        assertThat(key).startsWith("service");
        assertThat(key).endsWith("1700000040000");
    }

    @Test
    void detectsMinuteAggregatedMeasurements() {
        assertThat(TraceMetricMinuteBucket.requiresMinuteAggregation("service")).isTrue();
        assertThat(TraceMetricMinuteBucket.requiresMinuteAggregation("service.http")).isTrue();
        assertThat(TraceMetricMinuteBucket.requiresMinuteAggregation("service.trace")).isTrue();
        assertThat(TraceMetricMinuteBucket.requiresMinuteAggregation("service.flow")).isTrue();
        assertThat(TraceMetricMinuteBucket.requiresMinuteAggregation("service.exception")).isTrue();
        assertThat(TraceMetricMinuteBucket.requiresMinuteAggregation("service.instance")).isFalse();
        assertThat(TraceMetricMinuteBucket.requiresMinuteAggregation("service.http.connection.pool")).isFalse();
    }
}
