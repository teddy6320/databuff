package com.databuff.apm.common.cluster.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterCacheRegistryTest {

    @Test
    void regionIsSingletonPerName() {
        ClusterCacheRegistry registry = new ClusterCacheRegistry();
        ClusterCache first = registry.region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofMinutes(5));
        ClusterCache second = registry.region("ingest.meta", CacheRegionPolicy.PARTITIONED, Duration.ofMinutes(1));
        assertThat(second).isSameAs(first);
        assertThat(registry.get("ingest.meta")).isSameAs(first);
        assertThat(registry.get("missing")).isNull();
    }

    @Test
    void appliesReplicatorToNewRegions() {
        ClusterCacheRegistry registry = new ClusterCacheRegistry();
        RecordingReplicator replicator = new RecordingReplicator();
        registry.setReplicator(replicator);
        registry.region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1))
                .put("k", new byte[]{1});
        assertThat(replicator.puts).isEqualTo(1);
    }

    private static final class RecordingReplicator implements ClusterCacheReplicator {
        int puts;

        @Override
        public void replicatePut(String region, String key, byte[] value) {
            puts++;
        }
    }
}
