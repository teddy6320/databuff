package com.databuff.apm.ingest.pipeline.shard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoundRobinStrategyTest {

    @Test
    void cyclesThroughTasks() {
        RoundRobinStrategy strategy = new RoundRobinStrategy();
        strategy.init(3);
        assertThat(strategy.chooseTask("a")).isZero();
        assertThat(strategy.chooseTask("b")).isEqualTo(1);
        assertThat(strategy.chooseTask("c")).isEqualTo(2);
        assertThat(strategy.chooseTask("d")).isZero();
    }
}
