package com.databuff.apm.ingest.pipeline.task;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncTaskOverflowTest {

    private BlockingTask task;

    @AfterEach
    void tearDown() {
        if (task != null) {
            task.release();
            task.close();
        }
    }

    @Test
    void countsOverflowWhenRingBufferFull() throws InterruptedException {
        task = new BlockingTask(4);
        task.init();
        task.awaitReady();

        for (int i = 0; i < 16; i++) {
            task.handleEvent("k", i);
        }
        assertThat(task.overflowCount()).isGreaterThan(0);
    }

    private static final class BlockingTask extends AsyncTask {

        private final CountDownLatch processing = new CountDownLatch(1);
        private volatile boolean release;

        BlockingTask(int bufferSize) {
            super(bufferSize, 0);
        }

        void awaitReady() throws InterruptedException {
            processing.await(2, TimeUnit.SECONDS);
        }

        void release() {
            release = true;
        }

        @Override
        protected void processEvent(Object key, Object event) {
            processing.countDown();
            while (!release) {
                Thread.onSpinWait();
            }
        }
    }
}
