package com.databuff.apm.ingest.meta;

import com.databuff.apm.common.cluster.cache.ClusterCache;
import com.databuff.apm.common.cluster.cache.ClusterCacheRegistry;
import com.databuff.apm.common.meta.MetaServiceInfo;
import com.databuff.apm.common.model.DcSpan;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * Cross-span service metadata cache for ingest enrich.
 * Primary catalog comes from {@link MetaServiceRegistry}; local {@code ingest.meta}
 * region keeps hot service/instance hints on the ingest node.
 */
public final class IngestMetaCache {

    private final ClusterCache cache;
    private final MetaServiceRegistry serviceRegistry;

    public IngestMetaCache(ClusterCacheRegistry registry, MetaServiceRegistry serviceRegistry) {
        Objects.requireNonNull(registry);
        Objects.requireNonNull(serviceRegistry);
        ClusterCache region = registry.get("ingest.meta");
        if (region == null) {
            throw new IllegalStateException("ingest.meta cache region is not registered");
        }
        this.cache = region;
        this.serviceRegistry = serviceRegistry;
    }

    public void remember(DcSpan span) {
        if (span == null || span.serviceId == null || span.serviceId.isBlank()) {
            return;
        }
        MetaServiceInfo info = MetaServiceInfo.fromDcSpan(span);
        remember(span, info);
    }

    public void remember(DcSpan span, MetaServiceInfo info) {
        if (span == null || span.serviceId == null || span.serviceId.isBlank()) {
            return;
        }
        if (info != null) {
            serviceRegistry.remember(info);
        }
        if (span.service == null) {
            return;
        }
        String payload = span.service + "|" + Objects.toString(span.serviceInstance, "");
        cache.put(key(span.serviceId), payload.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<ServiceMeta> lookup(String serviceId) {
        if (serviceId == null || serviceId.isBlank()) {
            return Optional.empty();
        }
        Optional<MetaServiceInfo> catalog = serviceRegistry.getByServiceId(serviceId);
        if (catalog.isPresent()) {
            MetaServiceInfo info = catalog.get();
            String service = info.service();
            if (service == null || service.isBlank()) {
                service = info.name();
            }
            String instance = readInstanceFromLocalCache(serviceId);
            return Optional.of(new ServiceMeta(service == null ? "" : service, instance));
        }
        return readLocalServiceMeta(serviceId);
    }

    private Optional<ServiceMeta> readLocalServiceMeta(String serviceId) {
        byte[] raw = cache.get(key(serviceId));
        if (raw == null || raw.length == 0) {
            return Optional.empty();
        }
        String payload = new String(raw, StandardCharsets.UTF_8);
        int split = payload.indexOf('|');
        if (split < 0) {
            return Optional.of(new ServiceMeta(payload, ""));
        }
        return Optional.of(new ServiceMeta(payload.substring(0, split), payload.substring(split + 1)));
    }

    private String readInstanceFromLocalCache(String serviceId) {
        return readLocalServiceMeta(serviceId).map(ServiceMeta::serviceInstance).orElse("");
    }

    private static String key(String serviceId) {
        return "svc:" + serviceId;
    }

    public record ServiceMeta(String service, String serviceInstance) {
    }
}
