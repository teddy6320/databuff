package com.databuff.apm.web.monitor;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorRecordIdGeneratorTest {

    @Test
    void generatesPrefixedUniqueIds() {
        MonitorRecordIdGenerator generator = TestMonitorRecordIds.create();
        Set<String> alarmIds = new HashSet<>();
        Set<String> eventIds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            alarmIds.add(generator.nextAlarmId());
            eventIds.add(generator.nextEventId());
        }
        assertThat(alarmIds).hasSize(100).allMatch(id -> id.startsWith("A"));
        assertThat(eventIds).hasSize(100).allMatch(id -> id.startsWith("E"));
    }
}
