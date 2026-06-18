package com.databuff.apm.common.cluster.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecordingClusterPartialForwarderTest {

    @Test
    void recordsForwardedPayload() {
        RecordingClusterPartialForwarder forwarder = new RecordingClusterPartialForwarder();
        forwarder.forward("n1", "stream", "svc", 0, 60, new byte[] {1, 2});
        assertThat(forwarder.forwarded()).hasSize(1);
        assertThat(forwarder.forwarded().get(0).targetNodeId()).isEqualTo("n1");
    }
}
