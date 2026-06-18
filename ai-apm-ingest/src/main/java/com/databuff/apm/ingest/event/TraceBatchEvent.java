package com.databuff.apm.ingest.event;

import com.databuff.apm.common.model.DcSpan;

import java.util.List;

/**
 * Trace batch converted from one OTLP request and grouped by trace_id.
 */
public record TraceBatchEvent(List<DcSpan> spans) implements PipelineEvent {

    public TraceBatchEvent {
        spans = spans == null ? List.of() : List.copyOf(spans);
    }
}
