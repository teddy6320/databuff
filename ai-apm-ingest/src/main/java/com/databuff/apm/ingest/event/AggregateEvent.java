package com.databuff.apm.ingest.event;

import com.databuff.apm.common.model.OptimizedMetric;
import com.databuff.apm.ingest.component.AggregateComponent;
import com.databuff.apm.ingest.otel.OtlMetricLine;

import java.util.List;

/**
 * 指标聚合流水线事件，由 {@link AggregateComponent} 消费。
 * 四选一：单条 OptimizedMetric / trace 提取批量 / OTLP 行 / 转发 bytes。
 */
public record AggregateEvent(
        OptimizedMetric metric,
        List<OptimizedMetric> extractedMetrics,
        OtlMetricLine otlpLine,
        byte[] optimizedMetricBytes) implements PipelineEvent {

    public AggregateEvent(byte[] optimizedMetricBytes) {
        this(null, null, null, optimizedMetricBytes);
    }

    public static AggregateEvent fromMetric(OptimizedMetric metric) {
        return new AggregateEvent(metric, null, null, null);
    }

    /** trace fill 后批量提取的指标（service / service.flow / service.http …），走聚合 merge。 */
    public static AggregateEvent fromExtractedMetrics(List<OptimizedMetric> metrics) {
        return new AggregateEvent(null, List.copyOf(metrics), null, null);
    }

    public static AggregateEvent fromOtlp(OtlMetricLine line) {
        return new AggregateEvent(null, null, line, null);
    }

    /** 集群 ForwardPartial 收到的已序列化 partial。 */
    public static AggregateEvent fromBytes(byte[] bytes) {
        return new AggregateEvent(null, null, null, bytes);
    }
}
