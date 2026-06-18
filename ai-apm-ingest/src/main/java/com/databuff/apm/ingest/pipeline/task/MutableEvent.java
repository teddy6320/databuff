package com.databuff.apm.ingest.pipeline.task;

public final class MutableEvent {

    private Object key;
    private Object event;

    public void set(Object key, Object event) {
        this.key = key;
        this.event = event;
    }

    public Object getKey() {
        return key;
    }

    public Object getEvent() {
        return event;
    }

    public void clear() {
        key = null;
        event = null;
    }
}
