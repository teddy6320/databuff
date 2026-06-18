package com.databuff.apm.common.cluster.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ClusterCacheRegistry {

    private final Map<String, ClusterCache> regions = new ConcurrentHashMap<>();
    private ClusterCacheReplicator replicator = ClusterCacheReplicator.NOOP;
    private ClusterCacheTransport transport = ClusterCacheTransport.LOCAL;

    public void setReplicator(ClusterCacheReplicator replicator) {
        this.replicator = replicator == null ? ClusterCacheReplicator.NOOP : replicator;
    }

    public void setTransport(ClusterCacheTransport transport) {
        this.transport = transport == null ? ClusterCacheTransport.LOCAL : transport;
        regions.replaceAll((name, cache) -> new ClusterCache(
                cache.region(), cache.policy(), cache.ttl(), replicator, transport));
    }

    public ClusterCache region(String name, CacheRegionPolicy policy, Duration ttl) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(policy);
        return regions.computeIfAbsent(
                name,
                n -> new ClusterCache(n, policy, ttl, replicator, transport));
    }

    public ClusterCache get(String name) {
        return regions.get(name);
    }

}
