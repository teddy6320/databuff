package com.databuff.apm.common.cluster.leadership;

import java.util.Optional;

/** Single-node mode: local instance is always leader. */
public final class StandaloneClusterLeadership implements ClusterLeadership {

    private final String role;
    private final String localNodeId;

    public StandaloneClusterLeadership(String role, String localNodeId) {
        this.role = role == null || role.isBlank() ? "default" : role.trim();
        this.localNodeId = localNodeId == null || localNodeId.isBlank() ? "local" : localNodeId.trim();
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
        return true;
    }

    @Override
    public Optional<String> leaderNodeId() {
        return Optional.of(localNodeId);
    }

    @Override
    public void close() {
        // no resources
    }
}
