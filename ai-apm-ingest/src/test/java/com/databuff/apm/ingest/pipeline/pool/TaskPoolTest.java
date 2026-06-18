package com.databuff.apm.ingest.pipeline.pool;

import com.databuff.apm.ingest.pipeline.shard.HashShardingStrategy;
import com.databuff.apm.ingest.pipeline.task.Task;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskPoolTest {

    @Test
    void rejectsEmptyTasks() {
        assertThatThrownBy(() -> new TaskPool<>(new HashShardingStrategy()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dispatchesToTask() {
        AtomicInteger handled = new AtomicInteger();
        Task task = new CountingTask(handled);
        TaskPool<Task> pool = new TaskPool<>(new HashShardingStrategy(), task);
        pool.init();
        assertThat(pool.getTaskSize()).isEqualTo(1);
        assertThat(pool.handleEvent("k", "v")).isTrue();
        assertThat(handled.get()).isEqualTo(1);
        pool.close();
    }

    @Test
    void usesRoundRobinWhenStrategyNull() {
        AtomicInteger handled = new AtomicInteger();
        Task task = new CountingTask(handled);
        TaskPool<Task> pool = new TaskPool<>(null, task);
        pool.init();
        assertThat(pool.getTaskSize()).isEqualTo(1);
        pool.close();
    }

    private static final class CountingTask implements Task {

        private final AtomicInteger counter;

        CountingTask(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public void init() {
        }

        @Override
        public boolean handleEvent(Object key, Object event) {
            counter.incrementAndGet();
            return true;
        }

        @Override
        public void close() {
        }

        @Override
        public String getName() {
            return "counting";
        }
    }
}
