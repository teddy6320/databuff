package com.databuff.apm.common.cluster.coordination;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Live cluster partition view used for trace/metric sharding and ForwardPartial routing.
 * Implementations must read current membership on every call (no startup snapshots).
 */
public interface ClusterPartitionMembership {

    String localNodeId();

    List<String> sortedMembers();

    Map<String, String> endpointsByNodeId();

    /**
     * {@code true} when cluster mode is configured and more than one live member is registered.
     */
    boolean effectiveClusterEnabled();

    default Optional<String> endpointFor(String nodeId) {
        if (nodeId == null || nodeId.isBlank()) {
            return Optional.empty();
        }
        String endpoint = endpointsByNodeId().get(nodeId.trim());
        if (endpoint == null || endpoint.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(endpoint);
    }

    default String partitionOwner(String partitionKey) {
        if (!effectiveClusterEnabled()) {
            return localNodeId();
        }
        return ClusterPartitionRouter.chooseOwner(partitionKey, sortedMembers());
    }

    default boolean owns(String partitionKey) {
        if (!effectiveClusterEnabled()) {
            return true;
        }
        return localNodeId().equals(partitionOwner(partitionKey));
    }

    /**
     * When this node is not the partition owner, returns the peer that should receive the partial.
     * Empty when standalone, when this node owns the partition, or when the owner has no endpoint yet.
     */
    default Optional<String> forwardPartialTarget(String partitionKey) {
        if (!effectiveClusterEnabled() || owns(partitionKey)) {
            return Optional.empty();
        }
        String owner = partitionOwner(partitionKey);
        if (localNodeId().equals(owner)) {
            return Optional.empty();
        }
        return endpointFor(owner).map(ignored -> owner);
    }
}
