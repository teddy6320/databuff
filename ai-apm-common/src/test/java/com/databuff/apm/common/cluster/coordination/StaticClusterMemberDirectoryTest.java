package com.databuff.apm.common.cluster.coordination;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StaticClusterMemberDirectoryTest {

    @Test
    void localEndpointRegistersSelf() {
        StaticClusterMemberDirectory directory = StaticClusterMemberDirectory.local("n1", "127.0.0.1:18112");
        assertThat(directory.localNodeId()).isEqualTo("n1");
        assertThat(directory.endpointsByNodeId()).containsEntry("n1", "127.0.0.1:18112");
    }

    @Test
    void blankEndpointReturnsEmptyMap() {
        assertThat(StaticClusterMemberDirectory.local("n1", "").endpointsByNodeId()).isEmpty();
        assertThat(StaticClusterMemberDirectory.local("n1", null).endpointsByNodeId()).isEmpty();
    }
}
