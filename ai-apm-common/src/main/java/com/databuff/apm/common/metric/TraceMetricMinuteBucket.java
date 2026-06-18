package com.databuff.apm.common.metric;

import com.databuff.apm.common.model.OptimizedMetric;

import java.util.Arrays;
import java.util.Objects;

/** Minute-window aggregation for trace-derived metrics (excludes {@code service.instance}). */
public final class TraceMetricMinuteBucket {

    public static final long BUCKET_MS = 60_000L;

    private TraceMetricMinuteBucket() {
    }

    /** Trace 抽取指标走分钟聚合；{@code service.instance} 由注册表定时刷写，不走此路径。 */
    public static boolean requiresMinuteAggregation(String measurement) {
        return measurement != null
                && MetricSchemaRegistry.isTraceDerived(measurement)
                && !"service.instance".equals(measurement);
    }

    /** Floor span end time (nanos) to minute epoch millis. */
    public static long minuteBucketEpochMsFromEndNanos(long endTimeNanos) {
        long endMs = Math.max(0L, endTimeNanos / 1_000_000L);
        return (endMs / BUCKET_MS) * BUCKET_MS;
    }

    public static long minuteBucketEpochMs(long timestampNanos) {
        long ms = Math.max(0L, timestampNanos / 1_000_000L);
        return (ms / BUCKET_MS) * BUCKET_MS;
    }

    public static long minuteBucketEpochNanosFromEndNanos(long endTimeNanos) {
        return minuteBucketEpochMsFromEndNanos(endTimeNanos) * 1_000_000L;
    }

    /** tags + measurement + end-minute bucket — cluster partition / merge key. */
    public static String aggregationPartitionKey(OptimizedMetric metric) {
        Objects.requireNonNull(metric, "metric");
        long minuteMs = minuteBucketEpochMs(metric.timestamp());
        StringBuilder key = new StringBuilder(metric.measurement());
        for (String tag : metric.tagValues()) {
            key.append('\u0001').append(tag);
        }
        key.append('\u0001').append(minuteMs);
        return key.toString();
    }

    public static int aggregationTsId(OptimizedMetric metric, long minuteBucketEpochMs) {
        return Objects.hash(metric.measurement(), Arrays.hashCode(metric.tagValues()), minuteBucketEpochMs);
    }

    public static long currentMinuteEpochMs() {
        long nowMs = System.currentTimeMillis();
        return (nowMs / BUCKET_MS) * BUCKET_MS;
    }
}
