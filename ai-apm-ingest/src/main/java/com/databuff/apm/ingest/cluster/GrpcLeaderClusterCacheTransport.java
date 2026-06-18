package com.databuff.apm.ingest.cluster;

import com.databuff.apm.cluster.v1.ClusterCoordinationServiceGrpc;
import com.databuff.apm.cluster.v1.GetCacheRequest;
import com.databuff.apm.cluster.v1.GetCacheResponse;
import com.databuff.apm.cluster.v1.InvalidateCacheRequest;
import com.databuff.apm.cluster.v1.ReplicateCacheRequest;
import com.databuff.apm.common.cluster.cache.ClusterCacheTransport;
import com.databuff.apm.common.cluster.coordination.ClusterInstanceCoordinator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Followers proxy cache reads/writes to the ingest leader (Redis-like semantics). */
public final class GrpcLeaderClusterCacheTransport implements ClusterCacheTransport, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(GrpcLeaderClusterCacheTransport.class);

    private final ClusterInstanceCoordinator coordinator;
    private final String originNodeId;
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

    public GrpcLeaderClusterCacheTransport(ClusterInstanceCoordinator coordinator) {
        this.coordinator = coordinator;
        this.originNodeId = coordinator.localNodeId();
    }

    @Override
    public boolean leaderAuthoritative() {
        return coordinator.effectiveClusterEnabled();
    }

    @Override
    public boolean localLeader() {
        return coordinator.isLeader();
    }

    @Override
    public byte[] get(String region, String key) {
        GetCacheResponse response = leaderStub().getCache(GetCacheRequest.newBuilder()
                .setRegion(region)
                .setKey(key)
                .setOriginNodeId(originNodeId)
                .build());
        if (!response.getFound()) {
            return null;
        }
        return response.getValue().toByteArray();
    }

    @Override
    public void put(String region, String key, byte[] value) {
        leaderStub().replicateCache(ReplicateCacheRequest.newBuilder()
                .setRegion(region)
                .setKey(key)
                .setValue(com.google.protobuf.ByteString.copyFrom(value))
                .setOriginNodeId(originNodeId)
                .build());
    }

    @Override
    public void invalidate(String region, String key) {
        leaderStub().invalidateCache(InvalidateCacheRequest.newBuilder()
                .setRegion(region)
                .setKey(key)
                .setOriginNodeId(originNodeId)
                .build());
    }

    private ClusterCoordinationServiceGrpc.ClusterCoordinationServiceBlockingStub leaderStub() {
        String endpoint = coordinator.leaderEndpoint()
                .orElseThrow(() -> new IllegalStateException("ingest leader endpoint is unavailable"));
        return ClusterCoordinationServiceGrpc.newBlockingStub(channel(endpoint))
                .withDeadlineAfter(3, TimeUnit.SECONDS);
    }

    private ManagedChannel channel(String endpoint) {
        return channels.computeIfAbsent(endpoint, ep -> ManagedChannelBuilder.forTarget(ep)
                .usePlaintext()
                .build());
    }

    @Override
    public void close() {
        channels.values().forEach(ch -> {
            try {
                ch.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        channels.clear();
    }
}
