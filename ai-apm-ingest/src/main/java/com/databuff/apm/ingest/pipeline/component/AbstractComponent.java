package com.databuff.apm.ingest.pipeline.component;

import com.databuff.apm.ingest.pipeline.pool.TaskPool;
import com.databuff.apm.ingest.pipeline.task.Task;

public abstract class AbstractComponent<T extends Task> {

    private TaskPool<T> taskPool;
    private volatile boolean enabled = true;

    protected abstract String getName();

    public boolean emit(Object key, Object event) {
        if (!enabled || taskPool == null) {
            return false;
        }
        return taskPool.handleEvent(key, event);
    }

    public void init(TaskPool<T> taskPool) {
        this.taskPool = taskPool;
        this.taskPool.init();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected abstract TaskPool<T> generateTaskPool(int taskSize);

    public void start(int taskSize) {
        init(generateTaskPool(taskSize));
    }

    public void close() {
        if (taskPool != null) {
            taskPool.close();
        }
    }
}
