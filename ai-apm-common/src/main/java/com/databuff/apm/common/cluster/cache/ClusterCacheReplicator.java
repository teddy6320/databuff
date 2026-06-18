package com.databuff.apm.common.cluster.cache;

/**
 * Replicates cache mutations to peer ingest nodes via gRPC cluster coordination.
 */
public interface ClusterCacheReplicator {

    ClusterCacheReplicator NOOP = new ClusterCacheReplicator() {
    };

    default void replicatePut(String region, String key, byte[] value) {
    }

    default void replicateInvalidate(String region, String key) {
    }
}
