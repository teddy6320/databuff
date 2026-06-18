package com.databuff.apm.common.cluster.cache;

/**
 * Remote cache access for leader-authoritative regions.
 * Followers delegate to the elected leader; the leader uses {@link #LOCAL}.
 */
public interface ClusterCacheTransport {

    ClusterCacheTransport LOCAL = new ClusterCacheTransport() {
        @Override
        public boolean leaderAuthoritative() {
            return false;
        }

        @Override
        public boolean localLeader() {
            return true;
        }

        @Override
        public byte[] get(String region, String key) {
            return null;
        }

        @Override
        public void put(String region, String key, byte[] value) {
        }

        @Override
        public void invalidate(String region, String key) {
        }
    };

    boolean leaderAuthoritative();

    boolean localLeader();

    byte[] get(String region, String key);

    void put(String region, String key, byte[] value);

    void invalidate(String region, String key);
}
