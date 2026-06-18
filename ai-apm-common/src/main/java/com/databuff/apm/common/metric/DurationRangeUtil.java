package com.databuff.apm.common.metric;

/**
 * Maps span duration (nanoseconds) to HTTP latency buckets for {@code service.http} metrics.
 */
public final class DurationRangeUtil {

    private static final long[] UPPER_MS = {50, 100, 200, 500, 1_000, 3_000};

    private DurationRangeUtil() {
    }

    public static String bucket(long durationNanos) {
        long ms = durationNanos <= 0 ? 0 : durationNanos / 1_000_000L;
        long lower = 0;
        for (long upper : UPPER_MS) {
            if (ms <= upper) {
                return lower + "-" + upper + "ms";
            }
            lower = upper;
        }
        return lower + "ms+";
    }
}
