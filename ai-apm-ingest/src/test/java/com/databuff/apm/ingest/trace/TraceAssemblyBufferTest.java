package com.databuff.apm.ingest.trace;

import com.databuff.apm.common.model.DcSpan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class TraceAssemblyBufferTest {

    private final List<String> flushedTraceIds = new CopyOnWriteArrayList<>();
    private final List<Integer> flushedSpanCounts = new CopyOnWriteArrayList<>();
    private TraceAssemblyBuffer buffer;

    @AfterEach
    void tearDown() {
        if (buffer != null) {
            buffer.close();
        }
    }

    @Test
    void offersSpanWithoutTraceIdForImmediateFlush() {
        buffer = newBuffer(200L);
        DcSpan span = span("trace-a", "s1", "root");

        List<DcSpan> immediate = buffer.offer(spanWithoutTraceId("orphan"));

        assertThat(immediate).hasSize(1);
        assertThat(buffer.pendingTraces()).isZero();
    }

    @Test
    void flushesAfterRootArrivesAndTwoIdleChecks() {
        buffer = newBuffer(200L);
        buffer.offer(span("trace-root", "root", ""));
        buffer.offer(span("trace-root", "child", "root"));

        await().atMost(java.time.Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(flushedTraceIds).contains("trace-root");
            assertThat(flushedSpanCounts.get(flushedSpanCounts.size() - 1)).isEqualTo(2);
        });
    }

    @Test
    void flushesAfterFourIdleChecksWhenRootMissing() {
        buffer = newBuffer(200L);
        buffer.offer(span("trace-no-root", "child-a", "parent-a"));
        buffer.offer(span("trace-no-root", "child-b", "parent-b"));

        await().atMost(java.time.Duration.ofSeconds(3)).untilAsserted(() -> {
            assertThat(flushedTraceIds).contains("trace-no-root");
            assertThat(flushedSpanCounts.get(flushedSpanCounts.size() - 1)).isEqualTo(2);
        });
    }

    @Test
    void resetsIdleChecksWhenNewSpanArrives() {
        buffer = newBuffer(200L);
        buffer.offer(span("trace-delay", "root", ""));

        await().pollDelay(java.time.Duration.ofMillis(250)).atMost(java.time.Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(flushedTraceIds).isEmpty());

        buffer.offer(span("trace-delay", "child", "root"));

        await().pollDelay(java.time.Duration.ofMillis(250)).atMost(java.time.Duration.ofSeconds(1))
                .untilAsserted(() -> assertThat(flushedTraceIds).isEmpty());

        await().atMost(java.time.Duration.ofSeconds(2)).untilAsserted(() ->
                assertThat(flushedTraceIds).contains("trace-delay"));
    }

    @Test
    void offerAllBuffersBatchSpansForSameTrace() {
        buffer = newBuffer(200L);
        List<DcSpan> immediate = buffer.offerAll(List.of(
                span("trace-batch", "root", ""),
                span("trace-batch", "child", "root")));

        assertThat(immediate).isEmpty();
        await().atMost(java.time.Duration.ofSeconds(2)).untilAsserted(() ->
                assertThat(flushedSpanCounts).anyMatch(count -> count == 2));
    }

    @Test
    void flushAllDrainsPendingWithoutWaitingForTimer() {
        buffer = newBuffer(5_000L);
        buffer.offer(span("trace-close", "root", ""));

        List<List<DcSpan>> all = buffer.flushAll();

        assertThat(all).hasSize(1);
        assertThat(all.get(0)).hasSize(1);
        assertThat(buffer.pendingTraces()).isZero();
    }

    private TraceAssemblyBuffer newBuffer(long intervalMs) {
        flushedTraceIds.clear();
        flushedSpanCounts.clear();
        return new TraceAssemblyBuffer(intervalMs, (traceId, spans) -> {
            flushedTraceIds.add(traceId);
            flushedSpanCounts.add(spans.size());
        }, "trace-assembly-test");
    }

    private static DcSpan span(String traceId, String spanId, String parentId) {
        DcSpan span = new DcSpan();
        span.trace_id = traceId;
        span.span_id = spanId;
        span.parent_id = parentId;
        span.service = "demo";
        span.serviceId = "demo-id";
        span.name = spanId;
        span.resource = spanId;
        span.duration = 1;
        span.start = 1_700_000_000_000_000_000L;
        span.end = span.start + 1;
        return span;
    }

    private static DcSpan spanWithoutTraceId(String spanId) {
        DcSpan span = span("ignored", spanId, "");
        span.trace_id = "";
        return span;
    }
}
