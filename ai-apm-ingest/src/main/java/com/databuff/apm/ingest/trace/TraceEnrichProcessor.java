package com.databuff.apm.ingest.trace;

import com.databuff.apm.common.meta.MetaServiceInfo;
import com.databuff.apm.common.model.DcSpan;
import com.databuff.apm.ingest.meta.IngestMetaCache;
import com.databuff.apm.ingest.meta.MetaServiceCollector;
import com.databuff.apm.ingest.meta.ServiceInstanceRegistry;
import com.databuff.apm.ingest.trace.remote.RemoteCallProcessor;

/**
 * Step 2 · DTS enrich：聚合前字段补全（host / resource / service 元数据）。
 * <p>
 * 全程内存原地修改，不做 JSON 序列化/反序列化。
 */
public final class TraceEnrichProcessor {

    private final IngestMetaCache metaCache;
    private final MetaServiceCollector metaServiceCollector;
    private final ServiceInstanceRegistry serviceInstanceRegistry;
    private final RemoteCallProcessor remoteCallProcessor;

    public TraceEnrichProcessor() {
        this(null, null, null, null);
    }

    public TraceEnrichProcessor(IngestMetaCache metaCache) {
        this(metaCache, null, null, null);
    }

    public TraceEnrichProcessor(IngestMetaCache metaCache, MetaServiceCollector metaServiceCollector) {
        this(metaCache, metaServiceCollector, null, null);
    }

    public TraceEnrichProcessor(
            IngestMetaCache metaCache,
            MetaServiceCollector metaServiceCollector,
            ServiceInstanceRegistry serviceInstanceRegistry) {
        this(metaCache, metaServiceCollector, serviceInstanceRegistry, null);
    }

    public TraceEnrichProcessor(
            IngestMetaCache metaCache,
            MetaServiceCollector metaServiceCollector,
            ServiceInstanceRegistry serviceInstanceRegistry,
            RemoteCallProcessor remoteCallProcessor) {
        this.metaCache = metaCache;
        this.metaServiceCollector = metaServiceCollector;
        this.serviceInstanceRegistry = serviceInstanceRegistry;
        this.remoteCallProcessor = remoteCallProcessor;
    }

    public DcSpan enrich(DcSpan span) {
        if (span.hostName == null || span.hostName.isBlank()) {
            span.hostName = "unknown";
            span.host_id = "unknown";
        } else if (span.host_id == null || span.host_id.isBlank()) {
            span.host_id = span.hostName;
        }
        if (span.serviceInstance == null) {
            span.serviceInstance = "";
        }
        if (span.resource == null || span.resource.isBlank()) {
            span.resource = span.name == null ? "" : span.name;
        }
        MetaServiceInfo serviceInfo = null;
        if (metaCache != null) {
            if ((span.service == null || span.service.isBlank()) && span.serviceId != null) {
                metaCache.lookup(span.serviceId).ifPresent(meta -> {
                    span.service = meta.service();
                    if (span.serviceInstance == null || span.serviceInstance.isBlank()) {
                        span.serviceInstance = meta.serviceInstance();
                    }
                });
            }
            serviceInfo = MetaServiceInfo.fromDcSpan(span);
            metaCache.remember(span, serviceInfo);
        }
        if (metaServiceCollector != null) {
            if (serviceInfo == null) {
                serviceInfo = MetaServiceInfo.fromDcSpan(span);
            }
            metaServiceCollector.remember(serviceInfo);
        }
        if (serviceInstanceRegistry != null) {
            serviceInstanceRegistry.remember(span);
        }
        if (remoteCallProcessor != null) {
            remoteCallProcessor.enrichSpan(span);
        }
        return span;
    }
}
