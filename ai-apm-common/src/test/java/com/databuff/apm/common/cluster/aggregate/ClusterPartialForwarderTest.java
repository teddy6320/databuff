package com.databuff.apm.common.cluster.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ClusterPartialForwarderTest {

    @Test
    void noopForwarderDoesNothing() {
        assertThatCode(() -> ClusterPartialForwarder.NOOP.forward("n1", "stream", "key", 0, 60, new byte[] {1}))
                .doesNotThrowAnyException();
    }
}
