package com.databuff.apm.ingest.pipeline.task;

public interface Task {

    void init();

    void close();

    boolean handleEvent(Object key, Object event);

    String getName();
}
