package com.databuff.apm.ingest.pipeline.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncTaskTest {

    @Test
    void processesEventAfterInit() throws InterruptedException {
        AsyncTask task = new NoOpTask();
        task.init();
        assertThat(task.handleEvent("k", "v")).isTrue();
        Thread.sleep(50);
        task.close();
    }

    @Test
    void handleEventFailsBeforeInit() {
        AsyncTask task = new NoOpTask();
        assertThat(task.handleEvent("k", "v")).isFalse();
    }

    @Test
    void getNameIncludesIndex() {
        assertThat(new NoOpTask().getName()).contains("NoOpTask");
    }

    private static final class NoOpTask extends AsyncTask {

        NoOpTask() {
            super(8, 3);
        }

        @Override
        protected void processEvent(Object key, Object event) {
        }
    }
}
