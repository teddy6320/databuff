package com.databuff.apm.ingest.pipeline.shard;

import java.util.concurrent.atomic.AtomicInteger;

public final class RoundRobinStrategy implements ShardingStrategy {

    private final AtomicInteger counter = new AtomicInteger();
    private int taskSize = 1;

    @Override
    public void init(int taskSize) {
        this.taskSize = Math.max(1, taskSize);
    }

    @Override
    public int chooseTask(Object key) {
        return Math.floorMod(counter.getAndIncrement(), taskSize);
    }
}
