package com.databuff.apm.common.cluster.aggregate;

import com.databuff.apm.common.cluster.coordination.ClusterPartitionMembership;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;

/**
 * Stage-1 local merge for cluster aggregation streams.
 * Partition ownership and ForwardPartial routing live in {@link
 * ClusterPartitionMembership}.
 */
public final class ClusterAggregator {

    private final Map<String, BinaryOperator<byte[]>> mergers = new ConcurrentHashMap<>();
    private final Map<String, Map<String, byte[]>> localState = new ConcurrentHashMap<>();
    private final String localNodeId;

    public ClusterAggregator(String localNodeId) {
        this.localNodeId = Objects.requireNonNull(localNodeId);
    }

    public String localNodeId() {
        return localNodeId;
    }

    public void registerMerger(String stream, BinaryOperator<byte[]> merger) {
        mergers.put(stream, merger);
        localState.putIfAbsent(stream, new ConcurrentHashMap<>());
    }

    public void emitStage1(String stream, String partitionKey, long windowStart, long windowEnd, byte[] partial) {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(partitionKey);
        Objects.requireNonNull(partial);
        BinaryOperator<byte[]> merger = mergers.get(stream);
        if (merger == null) {
            throw new IllegalArgumentException("Unknown aggregation stream: " + stream);
        }
        String stateKey = partitionKey + "@" + windowStart + "-" + windowEnd;
        Map<String, byte[]> bucket = localState.get(stream);
        bucket.merge(stateKey, partial, merger);
    }

    public byte[] getMergedState(String stream, String partitionKey, long windowStart, long windowEnd) {
        Map<String, byte[]> bucket = localState.get(stream);
        if (bucket == null) {
            return null;
        }
        return bucket.get(partitionKey + "@" + windowStart + "-" + windowEnd);
    }

    public void clearWindow(String stream, String partitionKey, long windowStart, long windowEnd) {
        Map<String, byte[]> bucket = localState.get(stream);
        if (bucket != null) {
            bucket.remove(partitionKey + "@" + windowStart + "-" + windowEnd);
        }
    }
}
