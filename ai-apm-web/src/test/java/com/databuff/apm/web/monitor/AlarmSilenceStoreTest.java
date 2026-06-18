package com.databuff.apm.web.monitor;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmSilenceStoreTest {

    @Test
    void silencesServiceForDuration() {
        AlarmSilenceStore store = new AlarmSilenceStore();
        store.silenceService("demo-order", 30);
        assertThat(store.isSilenced("demo-order")).isTrue();
        assertThat(store.isSilenced("other")).isFalse();
    }

    @Test
    void ignoresInvalidSilenceRequest() {
        AlarmSilenceStore store = new AlarmSilenceStore();
        store.silenceService("", 30);
        store.silenceService("demo", 0);
        assertThat(store.isSilenced("demo")).isFalse();
        assertThat(store.isSilenced(null)).isFalse();
        assertThat(store.isSilenced("  ")).isFalse();
    }

    @Test
    void expiresSilence() throws Exception {
        AlarmSilenceStore store = new AlarmSilenceStore();
        store.silenceService("demo-order", 30);
        java.lang.reflect.Field field = AlarmSilenceStore.class.getDeclaredField("silencedUntil");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Instant> map = (Map<String, Instant>) field.get(store);
        map.put("demo-order", Instant.now().minusSeconds(30));
        assertThat(store.isSilenced("demo-order")).isFalse();
    }

    @Test
    void restoresSilenceFromDoris() {
        AlarmSilenceStore store = new AlarmSilenceStore();
        store.restore("demo", Instant.now().plusSeconds(600));
        assertThat(store.isSilenced("demo")).isTrue();
    }
}
