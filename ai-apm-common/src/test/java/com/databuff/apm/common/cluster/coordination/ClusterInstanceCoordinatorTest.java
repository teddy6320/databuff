package com.databuff.apm.common.cluster.coordination;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterInstanceCoordinatorTest {

    @Test
    void effectiveClusterDisabledForSingleMember() {
        try (ClusterInstanceCoordinator coordinator = ClusterInstanceCoordinator.create(
                new ClusterInstanceCoordinator.ClusterInstanceSettings(
                        "ingest",
                        "ingest-1",
                        "127.0.0.1:18112",
                        "",
                        true))) {
            assertThat(coordinator.clusterEnabled()).isTrue();
            assertThat(coordinator.effectiveClusterEnabled()).isFalse();
            assertThat(coordinator.isLeader()).isTrue();
            assertThat(coordinator.owns("trace-1")).isTrue();
        }
    }

    @Test
    void effectiveClusterEnabledForMultipleMembers() throws Exception {
        try (TestingServer zk = new TestingServer()) {
            String connect = zk.getConnectString();
            try (ClusterInstanceCoordinator coordinator = ClusterInstanceCoordinator.create(
                    new ClusterInstanceCoordinator.ClusterInstanceSettings(
                            "ingest",
                            "ingest-1",
                            "127.0.0.1:18112",
                            connect,
                            true));
                 ZkPeerHelper peer = new ZkPeerHelper(connect)) {
                peer.register("ingest-2", "127.0.0.1:18113");
                awaitMembers(coordinator, 2);
                assertThat(coordinator.effectiveClusterEnabled()).isTrue();
                assertThat(coordinator.sortedMembers()).containsExactly("ingest-1", "ingest-2");
            }
        }
    }

    @Test
    void effectiveClusterDisabledWhenClusterFlagOff() {
        try (ClusterInstanceCoordinator coordinator = ClusterInstanceCoordinator.create(
                new ClusterInstanceCoordinator.ClusterInstanceSettings(
                        "ingest",
                        "ingest-1",
                        "127.0.0.1:18112",
                        "",
                        false))) {
            assertThat(coordinator.effectiveClusterEnabled()).isFalse();
            assertThat(coordinator.isLeader()).isTrue();
            assertThat(coordinator.owns("trace-1")).isTrue();
        }
    }

    private static void awaitMembers(ClusterInstanceCoordinator coordinator, int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (coordinator.sortedMembers().size() >= expected) {
                return;
            }
            Thread.sleep(100);
        }
        assertThat(coordinator.sortedMembers()).hasSizeGreaterThanOrEqualTo(expected);
    }

    private static final class ZkPeerHelper implements AutoCloseable {
        private final org.apache.curator.framework.CuratorFramework client;

        private ZkPeerHelper(String connectString) throws Exception {
            client = org.apache.curator.framework.CuratorFrameworkFactory.newClient(
                    connectString, new org.apache.curator.retry.ExponentialBackoffRetry(500, 1));
            client.start();
            client.blockUntilConnected(5, java.util.concurrent.TimeUnit.SECONDS);
            client.createContainers(ZkMemberEntries.MEMBERS_ROOT);
        }

        private void register(String nodeId, String endpoint) throws Exception {
            client.create()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(ZkMemberEntries.memberPath(nodeId), endpoint.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() {
            client.close();
        }
    }
}
