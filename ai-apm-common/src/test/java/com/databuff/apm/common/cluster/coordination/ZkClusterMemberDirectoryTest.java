package com.databuff.apm.common.cluster.coordination;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZkClusterMemberDirectoryTest {

    @Test
    void fallsBackToStaticWithoutZk() {
        ClusterMemberDirectory directory = ZkClusterMemberDirectory.create(
                "ingest-1", "", "127.0.0.1:18112");
        assertThat(directory.localNodeId()).isEqualTo("ingest-1");
        assertThat(directory.endpointsByNodeId()).containsEntry("ingest-1", "127.0.0.1:18112");
    }

    @Test
    void fallsBackToLocalNodeWhenZkUnreachable() {
        ClusterMemberDirectory directory = ZkClusterMemberDirectory.create(
                "ingest-1", "127.0.0.1:1", "127.0.0.1:18112");
        assertThat(directory).isNotInstanceOf(ZkClusterMemberDirectory.class);
        assertThat(directory.endpointsByNodeId()).containsEntry("ingest-1", "127.0.0.1:18112");
    }

    @Test
    void discoversMembersFromZookeeper() throws Exception {
        try (TestingServer zk = new TestingServer()) {
            String connect = zk.getConnectString();
            try (ZkClusterMemberDirectory directory = openDirectory(
                    "ingest-1", connect, "10.0.0.1:18112");
                 CuratorHelper helper = new CuratorHelper(connect)) {
                helper.createMember("ingest-2", "10.0.0.2:18112");
                long deadline = System.currentTimeMillis() + 5000;
                while (System.currentTimeMillis() < deadline) {
                    if (directory.endpointsByNodeId().containsKey("ingest-2")) {
                        break;
                    }
                    Thread.sleep(100);
                }
                assertThat(directory.endpointsByNodeId())
                        .containsEntry("ingest-1", "10.0.0.1:18112")
                        .containsEntry("ingest-2", "10.0.0.2:18112");
            }
        }
    }

    @Test
    void parsesMemberChildData() {
        assertThat(ZkMemberEntries.memberPath("ingest-1")).endsWith("/ingest-1");
        ChildData first = new ChildData(
                ZkMemberEntries.memberPath("ingest-1"),
                null,
                "127.0.0.1:18112".getBytes(StandardCharsets.UTF_8));
        ChildData second = new ChildData(
                ZkMemberEntries.memberPath("ingest-2"),
                null,
                "127.0.0.1:18113".getBytes(StandardCharsets.UTF_8));
        assertThat(ZkMemberEntries.fromChildren(List.of(first, second)))
                .containsEntry("ingest-1", "127.0.0.1:18112")
                .containsEntry("ingest-2", "127.0.0.1:18113");
        assertThat(ZkMemberEntries.fromChildren(List.of())).isEmpty();
    }

    @Test
    void closesZkDirectory() throws Exception {
        try (TestingServer zk = new TestingServer()) {
            ZkClusterMemberDirectory directory = openDirectory(
                    "ingest-1", zk.getConnectString(), "10.0.0.1:18112");
            directory.close();
        }
    }

    @Test
    void skipsRegistrationWhenLocalEndpointMissing() throws Exception {
        try (TestingServer zk = new TestingServer()) {
            try (ZkClusterMemberDirectory directory = openDirectory("ingest-9", zk.getConnectString(), "")) {
                assertThat(directory.localNodeId()).isEqualTo("ingest-9");
                assertThat(directory.endpointsByNodeId()).isEmpty();
            }
        }
    }

    private static ZkClusterMemberDirectory openDirectory(
            String localNodeId, String connect, String localEndpoint) throws Exception {
        ClusterMemberDirectory created = ZkClusterMemberDirectory.create(localNodeId, connect, localEndpoint);
        assertThat(created).isInstanceOf(ZkClusterMemberDirectory.class);
        return (ZkClusterMemberDirectory) created;
    }

    private static final class CuratorHelper implements AutoCloseable {
        private final org.apache.curator.framework.CuratorFramework client;

        private CuratorHelper(String connectString) throws Exception {
            client = org.apache.curator.framework.CuratorFrameworkFactory.newClient(
                    connectString, new org.apache.curator.retry.ExponentialBackoffRetry(500, 1));
            client.start();
            client.blockUntilConnected(5, java.util.concurrent.TimeUnit.SECONDS);
            client.createContainers(ZkMemberEntries.MEMBERS_ROOT);
        }

        private void createMember(String nodeId, String endpoint) throws Exception {
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
