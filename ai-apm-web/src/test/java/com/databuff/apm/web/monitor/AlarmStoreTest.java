package com.databuff.apm.web.monitor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmStoreTest {

    @Test
    void opensAndResolvesEvents() {
        AlarmStore store = new AlarmStore(TestMonitorRecordIds.create());
        Alarm open = store.open("demo", EventRule.WAY_THRESHOLD, "warning", "msg");
        assertThat(store.findOpenByService("demo")).isPresent();
        store.resolveAllOpenByServiceAndDetectionWay("demo", EventRule.WAY_THRESHOLD);
        assertThat(store.findOpenByService("demo")).isEmpty();
        assertThat(store.listRecent(10).get(0).id()).isEqualTo(open.id()).startsWith("A");
    }

    @Test
    void findsOpenAlertByService() {
        AlarmStore store = new AlarmStore(TestMonitorRecordIds.create());
        store.open("checkout", EventRule.WAY_THRESHOLD, "warning", "msg");
        assertThat(store.findOpenByService("checkout")).isPresent();
        assertThat(store.findOpenByService("other")).isEmpty();
    }

    @Test
    void findsRecentResolvedWithinWindow() {
        AlarmStore store = new AlarmStore(TestMonitorRecordIds.create());
        Alarm resolved = store.open("checkout", EventRule.WAY_THRESHOLD, "warning", "msg")
                .resolve(java.time.Instant.now().minusSeconds(30));
        store.persistExisting(resolved);
        assertThat(store.findLastResolvedByService("checkout", 60_000)).isPresent();
        assertThat(store.findLastResolvedByService("checkout", 1_000)).isEmpty();
    }

    @Test
    void groupsOpenIncidentsByService() {
        AlarmStore store = new AlarmStore(TestMonitorRecordIds.create());
        store.open("checkout", EventRule.WAY_THRESHOLD, "warning", "msg-a");
        store.open("checkout", EventRule.WAY_MUTATION, "warning", "msg-b");
        store.open("billing", EventRule.WAY_THRESHOLD, "warning", "msg-c");
        assertThat(store.groupOpenIncidents()).hasSize(2);
        assertThat(store.groupOpenIncidents().get(0).service()).isEqualTo("checkout");
        assertThat(store.groupOpenIncidents().get(0).openCount()).isEqualTo(2);
    }
}
