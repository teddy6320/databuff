package com.databuff.apm.ingest.event;

import com.databuff.apm.common.model.DcSpan;

/**
 * Trace 流水线事件，携带 Step 1 转换后的 {@link DcSpan} 内存对象。
 * {@code skipEnrich=true} 表示集群转发已 enrich，owner 节点跳过 Step 2。
 */
public record TraceEvent(DcSpan span, boolean skipEnrich) implements PipelineEvent {

    public TraceEvent(DcSpan span) {
        this(span, false);
    }

    /** 集群 gRPC 收到转发 span 时使用，已在源节点完成 enrich。 */
    public static TraceEvent forwarded(DcSpan span) {
        return new TraceEvent(span, true);
    }
}
