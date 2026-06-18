package com.databuff.apm.common.cluster.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterCacheTest {

    @Test
    void putGetInvalidate() {
        ClusterCache cache = new ClusterCacheRegistry()
                .region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1));
        cache.put("k1", new byte[]{1, 2});
        assertThat(cache.get("k1")).containsExactly(1, 2);
        assertThat(cache.region()).isEqualTo("ingest.meta");
        assertThat(cache.policy()).isEqualTo(CacheRegionPolicy.REPLICATED);
        assertThat(cache.size()).isEqualTo(1);
        cache.invalidate("k1");
        assertThat(cache.get("k1")).isNull();
        assertThat(cache.size()).isZero();
    }

    @Test
    void expiresAfterTtl() throws InterruptedException {
        ClusterCache cache = new ClusterCache("tmp", CacheRegionPolicy.REPLICATED, Duration.ofMillis(50));
        cache.put("k", new byte[]{9});
        Thread.sleep(60);
        assertThat(cache.get("k")).isNull();
    }

    @Test
    void noTtlKeepsValue() {
        ClusterCache cache = new ClusterCache("tmp", CacheRegionPolicy.REPLICATED, null);
        cache.put("k", new byte[]{7});
        assertThat(cache.get("k")).containsExactly(7);
    }

    @Test
    void replicatesPutAndInvalidate() {
        RecordingReplicator replicator = new RecordingReplicator();
        ClusterCache cache = new ClusterCache("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1), replicator);
        cache.put("svc:demo", new byte[]{1});
        cache.invalidate("svc:demo");
        assertThat(replicator.puts).isEqualTo(1);
        assertThat(replicator.invalidates).isEqualTo(1);
    }

    @Test
    void appliesRemoteMutations() {
        ClusterCache cache = new ClusterCache("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1));
        cache.applyRemotePut("remote", new byte[]{3});
        assertThat(cache.get("remote")).containsExactly(3);
        cache.applyRemoteInvalidate("remote");
        assertThat(cache.get("remote")).isNull();
    }

    private static final class RecordingReplicator implements ClusterCacheReplicator {
        int puts;
        int invalidates;

        @Override
        public void replicatePut(String region, String key, byte[] value) {
            puts++;
        }

        @Override
        public void replicateInvalidate(String region, String key) {
            invalidates++;
        }
    }
}
