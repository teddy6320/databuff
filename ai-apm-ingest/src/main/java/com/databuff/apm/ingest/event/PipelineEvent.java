package com.databuff.apm.ingest.event;

public sealed interface PipelineEvent permits TraceEvent, TraceBatchEvent, MetricEvent, AggregateEvent {
}
