package com.databuff.apm.ingest.gateway;

import com.databuff.apm.ingest.component.MetricComponent;
import com.databuff.apm.ingest.component.TraceComponent;
import com.databuff.apm.ingest.event.MetricEvent;
import com.databuff.apm.ingest.event.PipelineEvent;
import com.databuff.apm.ingest.event.TraceBatchEvent;
import com.databuff.apm.ingest.event.TraceEvent;

/**
 * 流水线入口网关：按事件类型路由到 Trace / Metric 组件。
 * shardingKey 通常为 serviceKey，用于分片到对应 Task 线程。
 */
public final class PipelineGateway {

    private final TraceComponent traceComponent;
    private final MetricComponent metricComponent;

    public PipelineGateway(TraceComponent traceComponent, MetricComponent metricComponent) {
        this.traceComponent = traceComponent;
        this.metricComponent = metricComponent;
    }

    public boolean emit(Object shardingKey, PipelineEvent event) {
        if (event instanceof TraceEvent traceEvent) {
            return traceComponent.emit(shardingKey, traceEvent);
        }
        if (event instanceof TraceBatchEvent traceBatchEvent) {
            return traceComponent.emit(shardingKey, traceBatchEvent);
        }
        if (event instanceof MetricEvent metricEvent) {
            return metricComponent.emit(shardingKey, metricEvent);
        }
        return false;
    }
}
