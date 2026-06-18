package com.databuff.apm.web.persistence;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

@Component
public class AiMessagePersistenceQueue {

    private static final Logger log = LoggerFactory.getLogger(AiMessagePersistenceQueue.class);

    private final ApmReadRepository readRepository;
    private final String configDatabase;
    private final BlockingQueue<RoundFlushTask> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean workerRunning = new AtomicBoolean(false);
    private volatile BooleanSupplier persistenceReady = () -> false;

    public AiMessagePersistenceQueue(ApmReadRepository readRepository, ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.configDatabase = storageProperties.configDatabase();
    }

    void setPersistenceReady(BooleanSupplier persistenceReady) {
        this.persistenceReady = persistenceReady == null ? () -> false : persistenceReady;
    }

    void enqueueRound(String sessionId, List<ApmConfigRepository.AiMessageRow> rows) {
        if (sessionId == null || sessionId.isBlank() || rows == null || rows.isEmpty()) {
            return;
        }
        List<ApmConfigRepository.AiMessageRow> ordered = rows.stream()
                .sorted(Comparator
                        .comparingInt(ApmConfigRepository.AiMessageRow::roundIndex)
                        .thenComparingInt(ApmConfigRepository.AiMessageRow::messageIndex)
                        .thenComparing(ApmConfigRepository.AiMessageRow::createdAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ApmConfigRepository.AiMessageRow::messageId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        queue.offer(new RoundFlushTask(sessionId, ordered));
        drainAsync();
    }

    private void drainAsync() {
        if (!workerRunning.compareAndSet(false, true)) {
            return;
        }
        Thread worker = new Thread(this::drainLoop, "ai-message-persist");
        worker.setDaemon(true);
        worker.start();
    }

    private void drainLoop() {
        try {
            while (true) {
                RoundFlushTask task = queue.poll();
                if (task == null) {
                    return;
                }
                flushTask(task);
            }
        } finally {
            workerRunning.set(false);
            if (!queue.isEmpty()) {
                drainAsync();
            }
        }
    }

    private void flushTask(RoundFlushTask task) {
        if (!persistenceReady.getAsBoolean()) {
            log.debug("Skip AI message flush for {} because persistence is not ready", task.sessionId());
            return;
        }
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        List<ApmConfigRepository.AiMessageRow> failed = new ArrayList<>();
        for (ApmConfigRepository.AiMessageRow row : task.rows()) {
            try {
                repository.upsertAiMessage(row);
            } catch (Exception e) {
                failed.add(row);
                log.warn("Failed to persist AI message {} for session {}: {}",
                        row.messageId(), task.sessionId(), e.getMessage());
            }
        }
        if (!failed.isEmpty()) {
            log.warn("Dropped {} AI message(s) for session {} after persist failure",
                    failed.size(), task.sessionId());
        }
    }

    private record RoundFlushTask(String sessionId, List<ApmConfigRepository.AiMessageRow> rows) {
    }
}
