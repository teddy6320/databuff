package com.databuff.apm.ingest.pipeline.shard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashShardingStrategyTest {

    @Test
    void routesConsistentlyForSameKey() {
        HashShardingStrategy strategy = new HashShardingStrategy();
        strategy.init(4);
        assertThat(strategy.chooseTask("svc-a")).isEqualTo(strategy.chooseTask("svc-a"));
    }

    @Test
    void nullKeyRoutesToZero() {
        HashShardingStrategy strategy = new HashShardingStrategy();
        strategy.init(3);
        assertThat(strategy.chooseTask(null)).isZero();
    }
}
