package com.databuff.apm.common.cluster.coordination;

import com.databuff.apm.common.cluster.leadership.ClusterLeadership;
import com.databuff.apm.common.cluster.leadership.StandaloneClusterLeadership;
import com.databuff.apm.common.cluster.leadership.ZkClusterLeadership;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Shared cluster runtime: member directory + role-scoped leadership + task sharding helpers.
 */
public final class ClusterInstanceCoordinator implements ClusterPartitionMembership, AutoCloseable {

    private final String role;
    private final boolean clusterEnabled;
    private final ClusterMemberDirectory directory;
    private final ClusterLeadership leadership;

    private ClusterInstanceCoordinator(
            String role,
            boolean clusterEnabled,
            ClusterMemberDirectory directory,
            ClusterLeadership leadership) {
        this.role = role;
        this.clusterEnabled = clusterEnabled;
        this.directory = Objects.requireNonNull(directory);
        this.leadership = Objects.requireNonNull(leadership);
    }

    public static ClusterInstanceCoordinator create(ClusterInstanceSettings settings) {
        Objects.requireNonNull(settings, "settings");
        String localNodeId = settings.localNodeId();
        ClusterMemberDirectory directory = ZkClusterMemberDirectory.create(
                localNodeId,
                settings.role(),
                settings.zkConnectString(),
                settings.localEndpoint());
        ClusterLeadership leadership;
        if (settings.clusterEnabled()) {
            leadership = ZkClusterLeadership.create(settings.role(), localNodeId, settings.zkConnectString());
        } else {
            leadership = new StandaloneClusterLeadership(settings.role(), localNodeId);
        }
        return new ClusterInstanceCoordinator(settings.role(), settings.clusterEnabled(), directory, leadership);
    }

    public String role() {
        return role;
    }

    public boolean clusterEnabled() {
        return clusterEnabled;
    }

    /**
     * True when cluster mode is configured and more than one live member is registered.
     * Single-node deployments skip partition forwarding, leadership gating, and task sharding.
     */
    public boolean effectiveClusterEnabled() {
        return clusterEnabled && sortedMembers().size() > 1;
    }

    public String localNodeId() {
        return directory.localNodeId();
    }

    @Override
    public Map<String, String> endpointsByNodeId() {
        return directory.endpointsByNodeId();
    }

    @Override
    public List<String> sortedMembers() {
        List<String> members = ClusterTaskSharder.sortedMembers(directory.endpointsByNodeId());
        if (members.isEmpty()) {
            return List.of(directory.localNodeId());
        }
        return members;
    }

    public boolean isLeader() {
        if (!effectiveClusterEnabled()) {
            return true;
        }
        return leadership.isLeader();
    }

    public Optional<String> leaderNodeId() {
        return leadership.leaderNodeId();
    }

    public Optional<String> leaderEndpoint() {
        Optional<String> leaderNodeId = leaderNodeId();
        if (leaderNodeId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(directory.endpointsByNodeId().get(leaderNodeId.get()));
    }

    @Override
    public boolean owns(String itemKey) {
        if (!effectiveClusterEnabled()) {
            return true;
        }
        return ClusterTaskSharder.owns(itemKey, localNodeId(), sortedMembers());
    }

    public <T> List<T> filterOwned(List<T> items, Function<T, String> keyExtractor) {
        if (!effectiveClusterEnabled()) {
            return items == null ? List.of() : List.copyOf(items);
        }
        return ClusterTaskSharder.filterOwned(items, keyExtractor, localNodeId(), sortedMembers());
    }

    @Override
    public void close() {
        try {
            leadership.close();
        } catch (Exception ignored) {
            // best effort
        }
        if (directory instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // best effort
            }
        }
    }

    public record ClusterInstanceSettings(
            String role,
            String localNodeId,
            String localEndpoint,
            String zkConnectString,
            boolean clusterEnabled) {
    }
}
