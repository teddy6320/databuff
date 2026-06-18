package com.databuff.apm.ingest.metric;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JvmOtelMetricNormalizerTest {

    @Test
    void mapsOtelThreadCount() {
        assertThat(JvmOtelMetricNormalizer.normalizeIdentifier("jvm.threads.count", Map.of()))
                .contains("jvm.thread_count");
        assertThat(JvmOtelMetricNormalizer.normalizeIdentifier("jvm.thread.count", Map.of()))
                .contains("jvm.thread_count");
    }

    @Test
    void mapsHeapMemoryUsed() {
        assertThat(JvmOtelMetricNormalizer.normalizeIdentifier(
                "jvm.memory.used",
                Map.of("jvm.memory.type", "heap")))
                .contains("jvm.memory.heap.used");
    }

    @Test
    void mapsEdenPoolUsedToGcEdenSize() {
        assertThat(JvmOtelMetricNormalizer.normalizeIdentifier(
                "jvm.memory.used",
                Map.of("jvm.memory.type", "heap", "jvm.memory.pool.name", "G1 Eden Space")))
                .contains("jvm.gc.eden_size");
    }

    @Test
    void mapsDirectBufferUsed() {
        assertThat(JvmOtelMetricNormalizer.normalizeIdentifier(
                "jvm.buffer.memory.used",
                Map.of("jvm.buffer.pool.name", "direct")))
                .contains("jvm.buffer_pool.direct.used");
    }

    @Test
    void mapsGcDurationHistogramToMinorMetrics() {
        var normalized = JvmOtelMetricNormalizer.normalizeHistogram(
                "jvm.gc.duration",
                Map.of("jvm.gc.action", "end of minor GC"),
                0.42,
                7);
        assertThat(normalized).extracting(JvmOtelMetricNormalizer.NormalizedMetric::identifier)
                .containsExactly("jvm.gc.minor_collection_count", "jvm.gc.minor_collection_time");
        assertThat(normalized.get(0).value()).isEqualTo(7L);
        assertThat(normalized.get(1).value()).isEqualTo(0.42);
    }

    @Test
    void passesThroughDatabuffIdentifiers() {
        assertThat(JvmOtelMetricNormalizer.normalizeIdentifier("jvm.thread_count", Map.of()))
                .contains("jvm.thread_count");
    }

    @Test
    void mapsLegacyMemoryAttributes() {
        assertThat(JvmOtelMetricNormalizer.normalizeIdentifier(
                "process.runtime.jvm.memory.usage",
                Map.of("type", "heap", "pool", "PS Eden Space")))
                .contains("jvm.gc.eden_size");
    }
}
