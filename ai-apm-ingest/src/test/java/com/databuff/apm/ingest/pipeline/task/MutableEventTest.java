package com.databuff.apm.ingest.pipeline.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MutableEventTest {

    @Test
    void setAndClear() {
        MutableEvent event = new MutableEvent();
        event.set("key", "payload");
        assertThat(event.getKey()).isEqualTo("key");
        assertThat(event.getEvent()).isEqualTo("payload");
        event.clear();
        assertThat(event.getKey()).isNull();
        assertThat(event.getEvent()).isNull();
    }
}
