package com.databuff.apm.web.monitor.executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class MonitorTaskPool {

    private final ThreadPoolExecutor workerExecutor;

    public MonitorTaskPool(
            @Value("${apm.monitor.pool.core-size:4}") int coreSize,
            @Value("${apm.monitor.pool.max-size:16}") int maxSize,
            @Value("${apm.monitor.pool.queue-size:100}") int queueSize) {
        workerExecutor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void submit(Runnable task) {
        workerExecutor.execute(task);
    }

    /** Run tasks in parallel and block until all complete (or fail). */
    public void runAll(List<Runnable> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (Runnable task : tasks) {
            workerExecutor.execute(() -> {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
