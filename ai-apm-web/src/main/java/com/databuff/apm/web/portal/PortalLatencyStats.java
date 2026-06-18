package com.databuff.apm.web.portal;

import com.databuff.apm.common.query.ApmQueryModels.HttpLatencyBucketPoint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Builds portal-compatible latency histogram and percentile fields from Doris duration buckets. */
final class PortalLatencyStats {

    private static final Pattern RANGE_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*-\\s*(\\d+(?:\\.\\d+)?)\\s*(ns|µs|us|ms|s)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SINGLE_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(ns|µs|us|ms|s)?", Pattern.CASE_INSENSITIVE);

    private PortalLatencyStats() {
    }

    static Map<String, Object> fromBuckets(List<HttpLatencyBucketPoint> buckets) {
        Map<String, Long> histogram = buildHistogram(buckets);
        if (histogram.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> percentiles = computePercentiles(histogram);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("histogram", histogram);
        data.put("p50Latency", percentiles.get("p50Latency"));
        data.put("p75Latency", percentiles.get("p75Latency"));
        data.put("p90Latency", percentiles.get("p90Latency"));
        data.put("p95Latency", percentiles.get("p95Latency"));
        data.put("p99Latency", percentiles.get("p99Latency"));
        data.put("p100Latency", percentiles.get("p100Latency"));
        data.put("maxPercentile", percentiles.get("p100Latency"));
        return data;
    }

    private static Map<String, Long> buildHistogram(List<HttpLatencyBucketPoint> buckets) {
        Map<String, Long> histogram = new LinkedHashMap<>();
        for (HttpLatencyBucketPoint bucket : buckets) {
            long upperBound = parseDurationUpperBoundNs(bucket.durationRange());
            if (upperBound <= 0) {
                continue;
            }
            String key = String.valueOf(upperBound);
            histogram.merge(key, bucket.requestCount(), Long::sum);
        }
        return histogram;
    }

    private static Map<String, Long> computePercentiles(Map<String, Long> histogram) {
        List<Long> keys = histogram.keySet().stream().map(Long::parseLong).sorted().toList();
        long total = keys.stream().mapToLong(key -> histogram.get(String.valueOf(key))).sum();
        Map<String, Long> percentiles = new LinkedHashMap<>();
        if (total <= 0 || keys.isEmpty()) {
            percentiles.put("p50Latency", 0L);
            percentiles.put("p75Latency", 0L);
            percentiles.put("p90Latency", 0L);
            percentiles.put("p95Latency", 0L);
            percentiles.put("p99Latency", 0L);
            percentiles.put("p100Latency", 0L);
            return percentiles;
        }
        percentiles.put("p50Latency", percentile(keys, histogram, total, 0.5));
        percentiles.put("p75Latency", percentile(keys, histogram, total, 0.75));
        percentiles.put("p90Latency", percentile(keys, histogram, total, 0.9));
        percentiles.put("p95Latency", percentile(keys, histogram, total, 0.95));
        percentiles.put("p99Latency", percentile(keys, histogram, total, 0.99));
        percentiles.put("p100Latency", keys.get(keys.size() - 1));
        return percentiles;
    }

    private static long percentile(List<Long> keys, Map<String, Long> histogram, long total, double ratio) {
        double target = total * ratio;
        long cumulative = 0;
        for (long key : keys) {
            cumulative += histogram.get(String.valueOf(key));
            if (cumulative >= target) {
                return key;
            }
        }
        return keys.get(keys.size() - 1);
    }

    static long parseDurationUpperBoundNs(String range) {
        if (range == null || range.isBlank()) {
            return 0;
        }
        Matcher rangeMatch = RANGE_PATTERN.matcher(range.trim());
        if (rangeMatch.find()) {
            return durationToNs(Double.parseDouble(rangeMatch.group(2)), rangeMatch.group(3));
        }
        Matcher singleMatch = SINGLE_PATTERN.matcher(range.trim());
        if (singleMatch.find()) {
            return durationToNs(Double.parseDouble(singleMatch.group(1)), singleMatch.group(2));
        }
        return 0;
    }

    private static long durationToNs(double value, String unit) {
        if (unit == null || unit.isBlank()) {
            return (long) (value * 1_000_000);
        }
        return switch (unit.toLowerCase(Locale.ROOT)) {
            case "ns" -> (long) value;
            case "µs", "us" -> (long) (value * 1_000);
            case "ms" -> (long) (value * 1_000_000);
            case "s" -> (long) (value * 1_000_000_000);
            default -> (long) (value * 1_000_000);
        };
    }
}
