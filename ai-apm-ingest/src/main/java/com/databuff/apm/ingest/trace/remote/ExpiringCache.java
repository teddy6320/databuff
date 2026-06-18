package com.databuff.apm.ingest.trace.remote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple bounded TTL cache used by remote-service peer caches.
 */
final class ExpiringCache<V> {

    private final long ttlMs;
    private final int maxSize;
    private final Map<String, Entry<V>> store = new ConcurrentHashMap<>();

    ExpiringCache(long ttlMs, int maxSize) {
        this.ttlMs = Math.max(1L, ttlMs);
        this.maxSize = Math.max(1, maxSize);
    }

    V getIfPresent(String key) {
        if (key == null) {
            return null;
        }
        Entry<V> entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAtMs < System.currentTimeMillis()) {
            store.remove(key, entry);
            return null;
        }
        return entry.value;
    }

    void put(String key, V value) {
        if (key == null) {
            return;
        }
        if (store.size() >= maxSize) {
            evictOne();
        }
        store.put(key, new Entry<>(value, System.currentTimeMillis() + ttlMs));
    }

    private void evictOne() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Entry<V>> entry : store.entrySet()) {
            if (entry.getValue().expiresAtMs < now) {
                store.remove(entry.getKey(), entry.getValue());
                return;
            }
        }
        if (!store.isEmpty()) {
            store.keySet().stream().findFirst().ifPresent(store::remove);
        }
    }

    private record Entry<V>(V value, long expiresAtMs) {
    }
}
