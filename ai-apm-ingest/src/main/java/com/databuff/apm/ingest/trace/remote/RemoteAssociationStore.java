package com.databuff.apm.ingest.trace.remote;

import com.databuff.apm.common.cluster.cache.ClusterCache;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Remote association store; legacy ingest pipeline protect cache + Redis/Flink association.
 */
public final class RemoteAssociationStore {

    private static final long PROTECT_CACHE_TTL_MS = 24L * 60L * 60L * 1000L;
    private static final long LINKED_CACHE_TTL_MS = 72L * 60L * 60L * 1000L;
    private static final int MAX_SIZE = 20_000;
    private static final String DEFAULT_API_KEY = "default";

    private final ExpiringCache<Long> peerProtectCache = new ExpiringCache<>(PROTECT_CACHE_TTL_MS, MAX_SIZE);
    private final ExpiringCache<String> linkedPeerCache = new ExpiringCache<>(LINKED_CACHE_TTL_MS, MAX_SIZE);
    private final ClusterCache clusterCache;

    public RemoteAssociationStore(ClusterCache clusterCache) {
        this.clusterCache = clusterCache;
    }

    public boolean isConfirmedLinked(String apiKey, String peerKey) {
        String cacheKey = associationCacheKey(apiKey, peerKey);
        if (linkedPeerCache.getIfPresent(cacheKey) != null) {
            return true;
        }
        byte[] raw = clusterCache == null ? null : clusterCache.get(cacheKey);
        if (raw == null || raw.length == 0) {
            return false;
        }
        String value = new String(raw, StandardCharsets.UTF_8);
        if (!value.isBlank()) {
            linkedPeerCache.put(cacheKey, value);
            return true;
        }
        return false;
    }

    /**
     * @return {@code true} when peer is still inside first-seen protect window.
     */
    public boolean enterProtectPeriodIfNeeded(String peerKey, long protectTimeMs) {
        Long firstSeen = peerProtectCache.getIfPresent(peerKey);
        long now = System.currentTimeMillis();
        if (firstSeen == null) {
            peerProtectCache.put(peerKey, now);
            return true;
        }
        return now - firstSeen < protectTimeMs;
    }

    /**
     * @return association value: non-blank = linked internal service, blank = confirmed external.
     */
    public String readAssociation(String apiKey, String peerKey) {
        String cacheKey = associationCacheKey(apiKey, peerKey);
        String localLinked = linkedPeerCache.getIfPresent(cacheKey);
        if (localLinked != null) {
            return localLinked;
        }
        if (clusterCache == null) {
            return null;
        }
        byte[] raw = clusterCache.get(cacheKey);
        if (raw == null) {
            return null;
        }
        return new String(raw, StandardCharsets.UTF_8);
    }

    public void recordAssociation(String apiKey, String peerKey, String linkedValue) {
        String cacheKey = associationCacheKey(apiKey, peerKey);
        String value = linkedValue == null ? "" : linkedValue;
        if (!value.isBlank()) {
            linkedPeerCache.put(cacheKey, value);
        }
        if (clusterCache != null) {
            clusterCache.put(cacheKey, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String defaultApiKey() {
        return DEFAULT_API_KEY;
    }

    private static String associationCacheKey(String apiKey, String peerKey) {
        String resolvedApiKey = Objects.toString(apiKey, DEFAULT_API_KEY);
        if (resolvedApiKey.isBlank()) {
            resolvedApiKey = DEFAULT_API_KEY;
        }
        return "remote-assoc:" + resolvedApiKey + ":" + peerKey;
    }
}
