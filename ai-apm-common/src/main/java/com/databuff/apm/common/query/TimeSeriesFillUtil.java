package com.databuff.apm.common.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Aligns sparse Doris buckets to a fixed interval; missing points are {@code null}, not zero. */
public final class TimeSeriesFillUtil {

    private TimeSeriesFillUtil() {
    }

    public static int bucketSec(int intervalSec) {
        return Math.max(60, intervalSec);
    }

    public static long alignBucketEpochSec(long epochMillis, int intervalSec) {
        int bucketSec = bucketSec(intervalSec);
        return (epochMillis / 1000L / bucketSec) * bucketSec;
    }

    /** Last bucket label when {@code exclusiveEndMillis} is the portal endTime (right-open interval). */
    public static long lastInclusiveBucketEpochSec(long exclusiveEndMillis, int intervalSec) {
        return lastInclusiveBucketEpochSecWithStep(exclusiveEndMillis, bucketSec(intervalSec));
    }

    public static long alignBucketEpochSecWithStep(long epochMillis, long bucketSec) {
        if (bucketSec <= 0) {
            return epochMillis / 1000L;
        }
        return (epochMillis / 1000L / bucketSec) * bucketSec;
    }

    public static long lastInclusiveBucketEpochSecWithStep(long exclusiveEndMillis, long bucketSec) {
        if (bucketSec <= 0) {
            return exclusiveEndMillis / 1000L;
        }
        long bucketMillis = bucketSec * 1000L;
        if (exclusiveEndMillis % bucketMillis == 0) {
            return alignBucketEpochSecWithStep(exclusiveEndMillis, bucketSec) - bucketSec;
        }
        return alignBucketEpochSecWithStep(exclusiveEndMillis - 1, bucketSec);
    }

    public static List<Map<String, Object>> fillGroupedTimeSeriesRows(
            List<Map<String, Object>> rows,
            long fromMillis,
            long toMillis,
            long bucketSec,
            String epochColumn,
            List<String> groupColumns,
            List<String> metricColumns) {
        if (bucketSec <= 0) {
            return rows == null ? List.of() : rows;
        }
        long start = alignBucketEpochSecWithStep(fromMillis, bucketSec);
        long end = lastInclusiveBucketEpochSecWithStep(toMillis, bucketSec);
        if (start > end) {
            return List.of();
        }

        List<String> groups = groupColumns == null ? List.of() : groupColumns;
        List<String> metrics = metricColumns == null ? List.of() : metricColumns;
        String epochKey = epochColumn == null || epochColumn.isBlank() ? "epoch_sec" : epochColumn;

        Map<String, Map<Long, Map<String, Object>>> grouped = new LinkedHashMap<>();
        Map<String, Map<String, Object>> groupDimensions = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                if (row == null) {
                    continue;
                }
                String groupKey = groupSeriesKey(groups, row);
                long epoch = epochSec(row.get(epochKey));
                grouped.computeIfAbsent(groupKey, key -> new LinkedHashMap<>()).put(epoch, row);
                groupDimensions.putIfAbsent(groupKey, extractGroupValues(groups, row));
            }
        }
        if (groupDimensions.isEmpty()) {
            groupDimensions.put("", Map.of());
        }

        List<Map<String, Object>> filled = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> groupEntry : groupDimensions.entrySet()) {
            Map<Long, Map<String, Object>> rowByBucket =
                    grouped.getOrDefault(groupEntry.getKey(), Map.of());
            Map<String, Object> dimensions = groupEntry.getValue();
            for (long bucket = start; bucket <= end; bucket += bucketSec) {
                Map<String, Object> existing = rowByBucket.get(bucket);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(epochKey, bucket);
                for (String group : groups) {
                    row.put(group, dimensions.get(group));
                }
                for (String metric : metrics) {
                    row.put(metric, existing == null ? null : existing.get(metric));
                }
                filled.add(row);
            }
        }
        return filled;
    }

    private static String groupSeriesKey(List<String> groupColumns, Map<String, Object> row) {
        if (groupColumns.isEmpty()) {
            return "";
        }
        StringBuilder key = new StringBuilder();
        for (String group : groupColumns) {
            if (!key.isEmpty()) {
                key.append('\u0001');
            }
            key.append(group).append('=').append(String.valueOf(row.get(group)));
        }
        return key.toString();
    }

    private static Map<String, Object> extractGroupValues(List<String> groupColumns, Map<String, Object> row) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String group : groupColumns) {
            values.put(group, row.get(group));
        }
        return values;
    }

    private static long epochSec(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return Long.MIN_VALUE;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return Long.MIN_VALUE;
        }
    }

    public static List<ApmQueryModels.MetricSeriesPoint> fillMetricSeries(
            List<ApmQueryModels.MetricSeriesPoint> points,
            long fromMillis,
            long toMillis,
            int intervalSec) {
        int bucketSec = bucketSec(intervalSec);
        long start = alignBucketEpochSec(fromMillis, intervalSec);
        long end = lastInclusiveBucketEpochSec(toMillis, intervalSec);
        if (start > end) {
            return List.of();
        }

        Map<Long, Double> valueByBucket = new LinkedHashMap<>();
        if (points != null) {
            for (ApmQueryModels.MetricSeriesPoint point : points) {
                valueByBucket.put(point.epochSeconds(), point.value());
            }
        }

        List<ApmQueryModels.MetricSeriesPoint> filled = new ArrayList<>();
        for (long bucket = start; bucket <= end; bucket += bucketSec) {
            filled.add(new ApmQueryModels.MetricSeriesPoint(bucket, valueByBucket.get(bucket)));
        }
        return filled;
    }

    public static <T> List<List<Object>> fillEpochMsValues(
            Map<Long, T> valueByBucket,
            long fromMillis,
            long toMillis,
            int intervalSec,
            Function<T, Object> valueMapper) {
        int bucketSec = bucketSec(intervalSec);
        long start = alignBucketEpochSec(fromMillis, intervalSec);
        long end = lastInclusiveBucketEpochSec(toMillis, intervalSec);
        if (start > end) {
            return List.of();
        }

        List<List<Object>> values = new ArrayList<>();
        for (long bucket = start; bucket <= end; bucket += bucketSec) {
            T row = valueByBucket == null ? null : valueByBucket.get(bucket);
            Object value = row == null ? null : valueMapper.apply(row);
            values.add(Arrays.asList(bucket * 1000L, value));
        }
        return values;
    }

    public static Map<String, Number> fillStringKeyMap(
            Map<String, Number> existing,
            long fromMillis,
            long toMillis,
            int intervalSec) {
        return fillStringKeyObjectMap(existing, fromMillis, toMillis, intervalSec);
    }

    public static <V> Map<String, V> fillStringKeyObjectMap(
            Map<String, V> existing,
            long fromMillis,
            long toMillis,
            int intervalSec) {
        int bucketSec = bucketSec(intervalSec);
        long start = alignBucketEpochSec(fromMillis, intervalSec);
        long end = lastInclusiveBucketEpochSec(toMillis, intervalSec);
        if (start > end) {
            return Map.of();
        }

        Map<String, V> filled = new LinkedHashMap<>();
        for (long bucket = start; bucket <= end; bucket += bucketSec) {
            String key = String.valueOf(bucket * 1000L);
            V value = existing == null ? null : existing.get(key);
            filled.put(key, value);
        }
        return filled;
    }
}
