package com.databuff.apm.common.cluster.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/** Serializes cache mutations on the cluster leader (Redis-like single-writer semantics). */
public final class SerialTaskExecutor implements AutoCloseable {

    private static final long DEFAULT_TIMEOUT_SECONDS = 5;

    private final ExecutorService executor;

    public SerialTaskExecutor(String threadNamePrefix) {
        AtomicInteger sequence = new AtomicInteger();
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(threadNamePrefix + "-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        this.executor = Executors.newSingleThreadExecutor(factory);
    }

    public void run(Runnable task) {
        try {
            Future<?> future = executor.submit(task);
            future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Serial cache task interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException("Serial cache task failed", cause);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Serial cache task timed out", e);
        }
    }

    public <T> T call(Callable<T> task) {
        try {
            Future<T> future = executor.submit(task);
            return future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Serial cache task interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException("Serial cache task failed", cause);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Serial cache task timed out", e);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
