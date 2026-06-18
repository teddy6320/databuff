package com.databuff.apm.common.cluster.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process cache region. {@link CacheRegionPolicy#LEADER} delegates to {@link ClusterCacheTransport}.
 */
public final class ClusterCache {

    private final String region;
    private final CacheRegionPolicy policy;
    private final Duration ttl;
    private final ClusterCacheReplicator replicator;
    private final ClusterCacheTransport transport;
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    ClusterCache(String region, CacheRegionPolicy policy, Duration ttl) {
        this(region, policy, ttl, ClusterCacheReplicator.NOOP, ClusterCacheTransport.LOCAL);
    }

    ClusterCache(
            String region,
            CacheRegionPolicy policy,
            Duration ttl,
            ClusterCacheReplicator replicator) {
        this(region, policy, ttl, replicator, ClusterCacheTransport.LOCAL);
    }

    ClusterCache(
            String region,
            CacheRegionPolicy policy,
            Duration ttl,
            ClusterCacheReplicator replicator,
            ClusterCacheTransport transport) {
        this.region = Objects.requireNonNull(region);
        this.policy = Objects.requireNonNull(policy);
        this.ttl = ttl;
        this.replicator = replicator == null ? ClusterCacheReplicator.NOOP : replicator;
        this.transport = transport == null ? ClusterCacheTransport.LOCAL : transport;
    }

    public String region() {
        return region;
    }

    public CacheRegionPolicy policy() {
        return policy;
    }

    Duration ttl() {
        return ttl;
    }

    public void put(String key, byte[] value) {
        if (useLeaderTransport()) {
            transport.put(region, key, value);
            return;
        }
        putLocal(key, value);
        if (policy == CacheRegionPolicy.REPLICATED) {
            replicator.replicatePut(region, key, value);
        }
    }

    public byte[] get(String key) {
        if (useLeaderTransport()) {
            return transport.get(region, key);
        }
        return getLocal(key);
    }

    public void invalidate(String key) {
        if (useLeaderTransport()) {
            transport.invalidate(region, key);
            return;
        }
        store.remove(key);
        if (policy == CacheRegionPolicy.REPLICATED) {
            replicator.replicateInvalidate(region, key);
        }
    }

    /** Applies a remote replica without fan-out (legacy replicated mode). */
    public void applyRemotePut(String key, byte[] value) {
        putLocal(key, value);
    }

    public void applyRemoteInvalidate(String key) {
        store.remove(key);
    }

    public int size() {
        return store.size();
    }

    private boolean useLeaderTransport() {
        return policy == CacheRegionPolicy.LEADER
                && transport.leaderAuthoritative()
                && !transport.localLeader();
    }

    private void putLocal(String key, byte[] value) {
        store.put(key, new CacheEntry(value, System.currentTimeMillis()));
    }

    private byte[] getLocal(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            long ageMs = System.currentTimeMillis() - entry.updatedAt();
            if (ageMs > ttl.toMillis()) {
                store.remove(key, entry);
                return null;
            }
        }
        return entry.value();
    }

    record CacheEntry(byte[] value, long updatedAt) {
    }
}
