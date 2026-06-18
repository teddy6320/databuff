package com.databuff.apm.ingest.cluster;

import com.databuff.apm.common.cluster.cache.ClusterCache;
import com.databuff.apm.common.cluster.cache.ClusterCacheRegistry;
import com.databuff.apm.common.cluster.cache.SerialTaskExecutor;

/** Serial cache mutations served by the ingest leader over gRPC. */
public final class LeaderClusterCacheExecutor implements AutoCloseable {

    private final ClusterCacheRegistry cacheRegistry;
    private final SerialTaskExecutor serialExecutor;

    public LeaderClusterCacheExecutor(ClusterCacheRegistry cacheRegistry) {
        this.cacheRegistry = cacheRegistry;
        this.serialExecutor = new SerialTaskExecutor("ingest-leader-cache");
    }

    public void put(String region, String key, byte[] value) {
        serialExecutor.run(() -> applyPut(region, key, value));
    }

    public byte[] get(String region, String key) {
        return serialExecutor.call(() -> read(region, key));
    }

    public void invalidate(String region, String key) {
        serialExecutor.run(() -> applyInvalidate(region, key));
    }

    private void applyPut(String region, String key, byte[] value) {
        ClusterCache cache = cacheRegistry.get(region);
        if (cache != null) {
            cache.applyRemotePut(key, value);
        }
    }

    private byte[] read(String region, String key) {
        ClusterCache cache = cacheRegistry.get(region);
        if (cache == null) {
            return null;
        }
        return cache.get(key);
    }

    private void applyInvalidate(String region, String key) {
        ClusterCache cache = cacheRegistry.get(region);
        if (cache != null) {
            cache.applyRemoteInvalidate(key);
        }
    }

    @Override
    public void close() {
        serialExecutor.close();
    }
}
