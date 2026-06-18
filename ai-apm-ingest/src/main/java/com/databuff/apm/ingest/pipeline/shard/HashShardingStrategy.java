package com.databuff.apm.ingest.pipeline.shard;

public final class HashShardingStrategy implements ShardingStrategy {

    private int taskSize = 1;

    @Override
    public void init(int taskSize) {
        this.taskSize = Math.max(1, taskSize);
    }

    @Override
    public int chooseTask(Object key) {
        if (key == null) {
            return 0;
        }
        return Math.floorMod(key.hashCode(), taskSize);
    }
}
