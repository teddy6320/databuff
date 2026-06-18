package com.databuff.apm.common.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DorisBatchWriterTest {

    @Test
    void batchesAndFlushes() {
        DorisBatchWriter writer = new DorisBatchWriter(2);
        writer.offer(new byte[]{1});
        assertThat(writer.drainIfReady()).isEmpty();
        assertThat(writer.pendingCount()).isEqualTo(1);
        writer.offer(new byte[]{2});
        assertThat(writer.drainIfReady()).hasSize(2);
        assertThat(writer.pendingCount()).isZero();
        writer.offer(new byte[]{3});
        assertThat(writer.flushAll()).containsExactly(new byte[]{3});
        assertThat(writer.flushAll()).isEmpty();
    }
}
