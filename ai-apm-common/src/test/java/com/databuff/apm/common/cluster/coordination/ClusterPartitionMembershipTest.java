package com.databuff.apm.common.cluster.coordination;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterPartitionMembershipTest {

    @Test
    void standaloneOwnsEverythingAndDoesNotForward() {
        ClusterPartitionMembership membership = TestClusterMembership.standalone("n1");
        assertThat(membership.effectiveClusterEnabled()).isFalse();
        assertThat(membership.owns("trace-1")).isTrue();
        assertThat(membership.forwardPartialTarget("trace-1")).isEmpty();
    }

    @Test
    void fixedMembershipForwardsToPeerWhenNotOwner() {
        TestClusterMembership.Mutable membership = TestClusterMembership.mutable("n2");
        membership.setClusterEnabled(true);
        membership.setMembers(List.of("n1", "n2", "n3"));
        membership.setEndpoint("n1", "127.0.0.1:18112");
        membership.setEndpoint("n2", "127.0.0.1:18113");
        membership.setEndpoint("n3", "127.0.0.1:18114");

        String key = "partition-key";
        if (membership.owns(key)) {
            key = key + "-alt";
        }
        assertThat(membership.forwardPartialTarget(key)).isPresent();
    }

    @Test
    void fixedMembershipDoesNotForwardWithoutOwnerEndpoint() {
        TestClusterMembership.Mutable membership = TestClusterMembership.mutable("n2");
        membership.setClusterEnabled(true);
        membership.setMembers(List.of("n1", "n2"));
        membership.setEndpoint("n2", "127.0.0.1:18113");

        String key = "partition-key";
        if (membership.owns(key)) {
            key = key + "-alt";
        }
        assertThat(membership.forwardPartialTarget(key)).isEmpty();
    }

    @Test
    void fixedMembershipReflectsRuntimeMemberChanges() {
        TestClusterMembership.Mutable membership = TestClusterMembership.mutable("n1");
        assertThat(membership.effectiveClusterEnabled()).isFalse();
        assertThat(membership.forwardPartialTarget("trace-1")).isEmpty();

        membership.setClusterEnabled(true);
        membership.setMembers(List.of("n1", "n2"));
        membership.setEndpoint("n2", "127.0.0.1:18113");

        String key = "trace-1";
        if (membership.owns(key)) {
            key = key + "-alt";
        }
        assertThat(membership.effectiveClusterEnabled()).isTrue();
        assertThat(membership.forwardPartialTarget(key)).isPresent();
    }

    @Test
    void coordinatorUsesLiveMembershipForForwarding() throws Exception {
        try (ClusterInstanceCoordinator coordinator = ClusterInstanceCoordinator.create(
                new ClusterInstanceCoordinator.ClusterInstanceSettings(
                        "ingest",
                        "ingest-1",
                        "127.0.0.1:18112",
                        "",
                        true))) {
            assertThat(coordinator.effectiveClusterEnabled()).isFalse();
            assertThat(coordinator.forwardPartialTarget("trace-1")).isEmpty();
        }

        try (TestingServer zk = new TestingServer()) {
            try (ClusterInstanceCoordinator coordinator = ClusterInstanceCoordinator.create(
                    new ClusterInstanceCoordinator.ClusterInstanceSettings(
                            "ingest",
                            "ingest-1",
                            "127.0.0.1:18112",
                            zk.getConnectString(),
                            true));
                 ZkPeerHelper peer = new ZkPeerHelper(zk.getConnectString())) {
                peer.register("ingest-2", "127.0.0.1:18113");
                long deadline = System.currentTimeMillis() + 5000;
                while (System.currentTimeMillis() < deadline) {
                    if (coordinator.sortedMembers().size() >= 2) {
                        break;
                    }
                    Thread.sleep(100);
                }
                assertThat(coordinator.effectiveClusterEnabled()).isTrue();
                String key = "trace-1";
                if (coordinator.owns(key)) {
                    key = key + "-alt";
                }
                assertThat(coordinator.forwardPartialTarget(key)).contains("ingest-2");
            }
        }
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
