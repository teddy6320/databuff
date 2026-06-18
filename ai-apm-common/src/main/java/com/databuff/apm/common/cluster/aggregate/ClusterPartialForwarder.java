package com.databuff.apm.common.cluster.aggregate;

/**
 * Sends stage-1 aggregation partials to the partition owner (Step 3 cluster).
 */
@FunctionalInterface
public interface ClusterPartialForwarder {

    ClusterPartialForwarder NOOP = (targetNodeId, stream, partitionKey, windowStart, windowEnd, partial) -> {
    };

    void forward(
            String targetNodeId,
            String stream,
            String partitionKey,
            long windowStart,
            long windowEnd,
            byte[] partial);
}
