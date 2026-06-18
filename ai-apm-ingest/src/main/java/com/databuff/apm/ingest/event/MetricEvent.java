package com.databuff.apm.ingest.event;

import com.databuff.apm.common.model.OptimizedMetric;
import com.databuff.apm.common.serde.OptimizedMetricUtil;
import com.databuff.apm.ingest.otel.OtlMetricLine;

/**
 * Agent 直报 metric 事件。
 * {@link OtlMetricLine} 来自 OTLP；{@link OptimizedMetric} 来自已优化的指标格式。
 */
public record MetricEvent(OtlMetricLine otlpLine, OptimizedMetric optimizedMetric) implements PipelineEvent {

    public static MetricEvent fromOtlp(OtlMetricLine line) {
        return new MetricEvent(line, null);
    }

    public static MetricEvent fromOptimized(OptimizedMetric metric) {
        return new MetricEvent(null, metric);
    }

    public static MetricEvent fromOptimizedBytes(byte[] bytes) {
        return fromOptimized(OptimizedMetricUtil.deserialize(bytes));
    }
}
