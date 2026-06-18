package com.databuff.apm.web.portal;

import com.databuff.apm.common.time.ApmTimeZones;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

/** Parses portal-style datetime strings ({@code yyyy-MM-dd HH:mm:ss}) in {@link ApmTimeZones#SHANGHAI}. */
public final class PortalTimeParser {

    private static final long ONE_MINUTE_MS = 60_000L;

    private PortalTimeParser() {
    }

    /**
     * Doris metric query exclusive end at {@code nowMillis}:
     * {@code (nowMillis / 60000 * 60000) - 60000}. Window is half-open {@code [from, to)}.
     */
    public static long metricQueryEndMillis(long nowMillis) {
        return alignToMinuteFloor(nowMillis) - ONE_MINUTE_MS;
    }

    /**
     * Portal / Vuex globalTime convention and monitor rule evaluation query end.
     * E.g. at 13:52:10 → 13:51:00 (last complete minute bucket 13:50).
     */
    public static long portalEndNow() {
        return metricQueryEndMillis(System.currentTimeMillis());
    }

    /** Floor epoch millis to minute boundary (seconds and sub-second zeroed). */
    public static long alignToMinuteFloor(long millis) {
        return (millis / ONE_MINUTE_MS) * ONE_MINUTE_MS;
    }

    /**
     * Monitor rule evaluation Doris window {@code [from, to)}:
     * {@code to = metricQueryEndMillis(now)}, {@code from = to - lookbackMillis}.
     */
    public static long[] metricEvaluationRange(long lookbackMillis) {
        return metricEvaluationRange(System.currentTimeMillis(), lookbackMillis);
    }

    public static long[] metricEvaluationRange(long nowMillis, long lookbackMillis) {
        long to = metricQueryEndMillis(nowMillis);
        return new long[] {to - lookbackMillis, to};
    }

    /**
     * Event/alarm time for a half-open metric window {@code [from, to)}: the last complete minute
     * bucket before {@code evalToExclusive}. E.g. eval {@code to=14:49} → event {@code 14:48}.
     */
    public static Instant eventBucketInstant(long evalToExclusiveMillis) {
        return Instant.ofEpochMilli(evalToExclusiveMillis - ONE_MINUTE_MS);
    }

    public static Instant eventBucketNow() {
        return eventBucketInstant(portalEndNow());
    }

    /** Minute-aligned half-open {@code [from, to)} for Doris metric SQL. */
    public static long[] normalizeMetricQueryRange(long fromMillis, long toMillis) {
        long from = alignToMinuteFloor(fromMillis);
        long to = alignToMinuteFloor(toMillis);
        if (from > to) {
            from = to;
        }
        return new long[] {from, to};
    }

    static long portalFromFallback(long fallbackMillis) {
        long now = System.currentTimeMillis();
        long duration = Math.max(0L, now - fallbackMillis);
        return portalEndNow() - duration;
    }

    public static long parseMillis(Object value, long fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            long n = number.longValue();
            return n < 1_000_000_000_000L ? n * 1000L : n;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return fallback;
        }
        if (text.chars().allMatch(Character::isDigit)) {
            long n = Long.parseLong(text);
            return n < 1_000_000_000_000L ? n * 1000L : n;
        }
        try {
            return ApmTimeZones.wallClockToEpochMilli(text);
        } catch (DateTimeParseException ignored) {
            return Instant.parse(text).toEpochMilli();
        }
    }

    public static long rangeFrom(Map<String, Object> body, long fallback) {
        Object value = body.get("fromTime");
        if (value == null) {
            value = body.get("startTime");
        }
        if (value == null) {
            value = body.get("start");
        }
        if (isAbsent(value)) {
            return portalFromFallback(fallback);
        }
        return parseMillis(value, portalFromFallback(fallback));
    }

    public static long rangeTo(Map<String, Object> body, long fallback) {
        Object value = body.get("toTime");
        if (value == null) {
            value = body.get("endTime");
        }
        if (value == null) {
            value = body.get("end");
        }
        if (isAbsent(value)) {
            // Keep explicit future fallbacks (e.g. alarm windows) unchanged.
            if (fallback > System.currentTimeMillis()) {
                return fallback;
            }
            return portalEndNow();
        }
        return parseMillis(value, portalEndNow());
    }

    /** Raw portal datetime text for span SQL ({@code yyyy-MM-dd HH:mm:ss}), without epoch conversion. */
    public static String rangeFromText(Map<String, Object> body) {
        return rangeText(body, "fromTime", "startTime", "start");
    }

    /** Raw portal datetime text for span SQL ({@code yyyy-MM-dd HH:mm:ss}), without epoch conversion. */
    public static String rangeToText(Map<String, Object> body) {
        return rangeText(body, "toTime", "endTime", "end");
    }

    private static String rangeText(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object value = body.get(key);
            if (!isAbsent(value)) {
                return String.valueOf(value).trim();
            }
        }
        return null;
    }

    private static boolean isAbsent(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty();
    }
}
