package com.databuff.apm.common.cluster.leadership;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class ZkClusterLeadership implements ClusterLeadership, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ZkClusterLeadership.class);
    private static final int CONNECT_TIMEOUT_SECONDS = 5;

    private final String role;
    private final String localNodeId;
    private final CuratorFramework client;
    private final LeaderLatch latch;
    private volatile boolean leader;

    private ZkClusterLeadership(String role, String localNodeId, CuratorFramework client, LeaderLatch latch) {
        this.role = role;
        this.localNodeId = localNodeId;
        this.client = client;
        this.latch = latch;
    }

    public static ClusterLeadership create(String role, String localNodeId, String zkConnectString) {
        if (zkConnectString == null || zkConnectString.isBlank()) {
            return new StandaloneClusterLeadership(role, localNodeId);
        }
        try {
            return connect(role, localNodeId, zkConnectString);
        } catch (Exception e) {
            log.warn("ZK leadership unavailable for role {} ({}), using standalone leadership", role, e.getMessage());
            return new StandaloneClusterLeadership(role, localNodeId);
        }
    }

    private static ZkClusterLeadership connect(String role, String localNodeId, String zkConnectString) throws Exception {
        String normalizedRole = role == null || role.isBlank() ? "default" : role.trim();
        String normalizedNodeId = localNodeId == null || localNodeId.isBlank() ? "local" : localNodeId.trim();
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                zkConnectString.trim(),
                new ExponentialBackoffRetry(500, 3));
        client.start();
        if (!client.blockUntilConnected(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            client.close();
            throw new IllegalStateException("ZK connect timeout");
        }
        String latchPath = LeadershipPathEntries.leaderLatchPath(normalizedRole);
        LeaderLatch latch = new LeaderLatch(client, latchPath, normalizedNodeId);
        ZkClusterLeadership leadership = new ZkClusterLeadership(normalizedRole, normalizedNodeId, client, latch);
        latch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                leadership.leader = true;
                log.info("Became cluster leader role={} nodeId={}", normalizedRole, normalizedNodeId);
            }

            @Override
            public void notLeader() {
                leadership.leader = false;
                log.info("Relinquished cluster leadership role={} nodeId={}", normalizedRole, normalizedNodeId);
            }
        });
        latch.start();
        leadership.leader = latch.hasLeadership();
        log.info("ZK cluster leadership ready role={} nodeId={} path={} isLeader={}",
                normalizedRole, normalizedNodeId, latchPath, leadership.leader);
        return leadership;
    }

    @Override
    public String role() {
        return role;
    }

    @Override
    public String localNodeId() {
        return localNodeId;
    }

    @Override
    public boolean isLeader() {
        return leader;
    }

    @Override
    public Optional<String> leaderNodeId() {
        try {
            Participant participant = latch.getLeader();
            if (participant == null || participant.getId() == null || participant.getId().isBlank()) {
                return isLeader() ? Optional.of(localNodeId) : Optional.empty();
            }
            return Optional.of(participant.getId());
        } catch (Exception e) {
            return isLeader() ? Optional.of(localNodeId) : Optional.empty();
        }
    }

    @Override
    public void close() {
        try {
            latch.close();
        } catch (Exception ignored) {
            // best effort
        }
        try {
            client.close();
        } catch (Exception ignored) {
            // best effort
        }
    }

}
