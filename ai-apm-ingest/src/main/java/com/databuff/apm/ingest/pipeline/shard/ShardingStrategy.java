package com.databuff.apm.ingest.pipeline.shard;

public interface ShardingStrategy {

    void init(int taskSize);

    int chooseTask(Object key);
}
