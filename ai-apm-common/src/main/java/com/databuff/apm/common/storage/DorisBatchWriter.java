package com.databuff.apm.common.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Batches rows for Doris Stream Load (HTTP implementation in Step 1).
 */
public class DorisBatchWriter {

    private final List<byte[]> pending = new ArrayList<>();
    private final int maxBatchSize;

    public DorisBatchWriter(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public synchronized void offer(byte[] row) {
        pending.add(row);
    }

    public synchronized void offerAll(List<byte[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        pending.addAll(rows);
    }

    public synchronized List<byte[]> drainIfReady() {
        if (pending.size() < maxBatchSize) {
            return List.of();
        }
        List<byte[]> batch = new ArrayList<>(pending);
        pending.clear();
        return batch;
    }

    public synchronized List<byte[]> flushAll() {
        if (pending.isEmpty()) {
            return List.of();
        }
        List<byte[]> batch = new ArrayList<>(pending);
        pending.clear();
        return batch;
    }

    public synchronized int pendingCount() {
        return pending.size();
    }
}
