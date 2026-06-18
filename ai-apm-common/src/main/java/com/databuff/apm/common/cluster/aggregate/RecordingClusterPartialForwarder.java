package com.databuff.apm.common.cluster.aggregate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Records forwarded partials without network I/O (tests / dev).
 */
public final class RecordingClusterPartialForwarder implements ClusterPartialForwarder {

    private final List<ForwardedPartial> forwarded = new CopyOnWriteArrayList<>();

    @Override
    public void forward(
            String targetNodeId,
            String stream,
            String partitionKey,
            long windowStart,
            long windowEnd,
            byte[] partial) {
        forwarded.add(new ForwardedPartial(
                targetNodeId, stream, partitionKey, windowStart, windowEnd, partial.clone()));
    }

    public List<ForwardedPartial> forwarded() {
        return Collections.unmodifiableList(new ArrayList<>(forwarded));
    }

    public void clear() {
        forwarded.clear();
    }

    public record ForwardedPartial(
            String targetNodeId,
            String stream,
            String partitionKey,
            long windowStart,
            long windowEnd,
            byte[] partial) {
    }
}
