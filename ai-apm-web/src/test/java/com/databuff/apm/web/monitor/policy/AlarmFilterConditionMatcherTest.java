package com.databuff.apm.web.monitor.policy;

import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.pipeline.EventRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmFilterConditionMatcherTest {

    @Test
    void emptyConditionsMatchAll() {
        Alarm alarm = sampleAlarm("checkout error");
        EventRecord event = sampleEvent("checkout errors");

        assertThat(AlarmFilterConditionMatcher.matches(List.of(), alarm, event)).isTrue();
    }

    @Test
    void matchesEqualsNotEqualsContainsAndNotContains() {
        Alarm alarm = sampleAlarm("checkout error rate 10%");
        EventRecord event = sampleEvent("checkout errors");

        assertThat(AlarmFilterConditionMatcher.matchOperator("abc", "=", "abc")).isTrue();
        assertThat(AlarmFilterConditionMatcher.matchOperator("abc", "!=", "xyz")).isTrue();
        assertThat(AlarmFilterConditionMatcher.matchOperator("checkout error", "like", "error")).isTrue();
        assertThat(AlarmFilterConditionMatcher.matchOperator("checkout error", "notLike", "slow")).isTrue();

        List<?> conditions = List.of(
                Map.of("connector", "AND", "left", "description", "operator", "like", "right", "error"),
                Map.of("connector", "AND", "left", "ruleName", "operator", "=", "right", "checkout errors"),
                Map.of("connector", "AND", "left", "description", "operator", "notLike", "right", "slow"));

        assertThat(AlarmFilterConditionMatcher.matches(conditions, alarm, event)).isTrue();
    }

    @Test
    void supportsOrConnector() {
        Alarm alarm = sampleAlarm("slow checkout");
        EventRecord event = sampleEvent("latency rule");

        List<?> conditions = List.of(
                Map.of("connector", "OR", "left", "description", "operator", "=", "right", "slow checkout"),
                Map.of("connector", "OR", "left", "ruleName", "operator", "=", "right", "error rule"));

        assertThat(AlarmFilterConditionMatcher.matches(conditions, alarm, event)).isTrue();
    }

    private static Alarm sampleAlarm(String message) {
        Instant now = Instant.parse("2026-06-18T10:00:00Z");
        return new Alarm(
                "A1",
                0L,
                "checkout",
                EventRule.WAY_THRESHOLD,
                "critical",
                message,
                Alarm.STATUS_RESOLVED,
                now,
                now);
    }

    private static EventRecord sampleEvent(String ruleName) {
        return new EventRecord(
                "E1",
                1L,
                ruleName,
                "checkout",
                EventRule.WAY_THRESHOLD,
                "critical",
                EventRecord.STATUS_TRIGGER,
                "checkout error rate 10%",
                "checkout",
                false,
                Instant.parse("2026-06-18T10:00:00Z"));
    }
}
