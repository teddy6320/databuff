package com.databuff.apm.common.cluster.cache;

public enum CacheRegionPolicy {
    LOCAL,
    /** @deprecated use {@link #LEADER} in cluster mode */
    REPLICATED,
    PARTITIONED,
    /** Leader holds authoritative cache; followers proxy reads/writes through the leader. */
    LEADER
}
