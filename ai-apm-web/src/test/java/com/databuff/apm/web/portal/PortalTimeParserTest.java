package com.databuff.apm.web.portal;

import com.databuff.apm.common.time.ApmTimeZones;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PortalTimeParserTest {

    @Test
    void portalEndNowMatchesFrontendGlobalTimeExclusiveEnd() {
        long now = LocalDateTime.of(2026, 6, 5, 20, 36, 15)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant()
                .toEpochMilli();
        long expected = LocalDateTime.of(2026, 6, 5, 20, 35, 0)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant()
                .toEpochMilli();

        assertThat(PortalTimeParser.metricQueryEndMillis(now)).isEqualTo(expected);
        assertThat((now / 60_000L) * 60_000L - 60_000L).isEqualTo(expected);
    }

    @Test
    void metricQueryEndAt135210UsesFormulaAndHalfOpenWindow() {
        long now = LocalDateTime.of(2026, 6, 9, 13, 52, 10)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant()
                .toEpochMilli();
        long expectedTo = LocalDateTime.of(2026, 6, 9, 13, 51, 0)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant()
                .toEpochMilli();
        long expectedFrom = LocalDateTime.of(2026, 6, 9, 13, 46, 0)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant()
                .toEpochMilli();

        assertThat(PortalTimeParser.metricQueryEndMillis(now)).isEqualTo(expectedTo);
        assertThat((now / 60_000L) * 60_000L - 60_000L).isEqualTo(expectedTo);

        long[] range = PortalTimeParser.metricEvaluationRange(now, 5 * 60_000L);
        assertThat(range[1]).isEqualTo(expectedTo);
        assertThat(range[0]).isEqualTo(expectedFrom);
    }

    @Test
    void rangeToUsesPortalEndWhenMissing() {
        long end = PortalTimeParser.rangeTo(Map.of(), System.currentTimeMillis());
        long expected = PortalTimeParser.portalEndNow();
        assertThat(end).isEqualTo(expected);
        assertThat(end % 60_000L).isZero();
    }

    @Test
    void rangeToKeepsExplicitEndTime() {
        long end = PortalTimeParser.rangeTo(
                Map.of("endTime", "2026-06-04 12:00:00"), 0L);
        long expected = LocalDateTime.of(2026, 6, 4, 12, 0, 0)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant()
                .toEpochMilli();
        assertThat(end).isEqualTo(expected);
    }

    @Test
    void rangeFromTextReturnsPortalDatetimeLiteral() {
        assertThat(PortalTimeParser.rangeFromText(Map.of("fromTime", "2026-06-05 14:00:00")))
                .isEqualTo("2026-06-05 14:00:00");
        assertThat(PortalTimeParser.rangeToText(Map.of("toTime", "2026-06-05 14:01:00")))
                .isEqualTo("2026-06-05 14:01:00");
    }

    @Test
    void rangeFromUsesPortalDurationWhenMissing() {
        long now = System.currentTimeMillis();
        long from = PortalTimeParser.rangeFrom(Map.of(), now - 3_600_000L);
        long to = PortalTimeParser.rangeTo(Map.of(), now);
        assertThat(to - from).isEqualTo(3_600_000L);
        assertThat(from % 60_000L).isZero();
        assertThat(to % 60_000L).isZero();
    }

    @Test
    void metricEvaluationRangeAlignsToMinuteBoundaries() {
        long lookback = 5 * 60_000L;
        long[] range = PortalTimeParser.metricEvaluationRange(lookback);
        assertThat(range[1]).isEqualTo(PortalTimeParser.portalEndNow());
        assertThat(range[1] - range[0]).isEqualTo(lookback);
        assertThat(range[0] % 60_000L).isZero();
        assertThat(range[1] % 60_000L).isZero();
    }

    @Test
    void eventBucketInstantIsMinuteBeforeEvalExclusiveEnd() {
        long evalTo = LocalDateTime.of(2026, 6, 9, 14, 49, 0)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant()
                .toEpochMilli();
        Instant bucket = PortalTimeParser.eventBucketInstant(evalTo);
        Instant expected = LocalDateTime.of(2026, 6, 9, 14, 48, 0)
                .atZone(ApmTimeZones.SHANGHAI)
                .toInstant();
        assertThat(bucket).isEqualTo(expected);
    }

    @Test
    void normalizeMetricQueryRangeFloorsBounds() {
        long[] range = PortalTimeParser.normalizeMetricQueryRange(1_005L, 3_609_999L);
        assertThat(range[0]).isEqualTo(0L);
        assertThat(range[1]).isEqualTo(3_600_000L);
    }
}
