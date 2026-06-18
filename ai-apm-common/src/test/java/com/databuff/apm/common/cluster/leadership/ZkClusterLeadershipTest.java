package com.databuff.apm.common.cluster.leadership;

import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZkClusterLeadershipTest {

    @Test
    void fallsBackToStandaloneWithoutZk() throws Exception {
        ClusterLeadership leadership = ZkClusterLeadership.create("web", "web-1", "");
        assertThat(leadership).isInstanceOf(StandaloneClusterLeadership.class);
        assertThat(leadership.isLeader()).isTrue();
        assertThat(leadership.leaderNodeId()).contains("web-1");
        leadership.close();
    }

    @Test
    void electsSingleLeaderAmongTwoParticipants() throws Exception {
        try (TestingServer zk = new TestingServer()) {
            String connect = zk.getConnectString();
            try (ClusterLeadership first = open("web", "web-1", connect);
                 ClusterLeadership second = open("web", "web-2", connect)) {
                awaitLeadership(first);
                awaitLeadership(second);
                assertThat(first.isLeader() ^ second.isLeader()).isTrue();
            }
        }
    }

    private static ClusterLeadership open(String role, String nodeId, String connect) throws Exception {
        ClusterLeadership leadership = ZkClusterLeadership.create(role, nodeId, connect);
        assertThat(leadership).isInstanceOf(ZkClusterLeadership.class);
        return leadership;
    }

    private static void awaitLeadership(ClusterLeadership leadership) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (leadership.leaderNodeId().isPresent()) {
                return;
            }
            Thread.sleep(100);
        }
    }
}
