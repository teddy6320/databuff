package com.databuff.apm.ingest.otel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Step 1 产物：OTLP metric 的 DataBuff 内存行。
 * 仅在集群 gRPC 转发或最终写 Doris 时才 {@link #toJsonBytes()} 序列化。
 */
public record OtlMetricLine(
        long tsMillis,
        String serviceId,
        String service,
        String metric,
        Number value,
        String serviceInstance,
        String tagHost,
        String threadPoolName,
        String objectPoolName,
        String httpConnectionPoolName,
        String connectionPoolName,
        String poolName,
        String resourceMeta) {

    private static final ObjectMapper JSON = new ObjectMapper();

    public byte[] toJsonBytes() throws JsonProcessingException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("ts", tsMillis);
        row.put("service_id", serviceId);
        row.put("service", service);
        row.put("metric", metric);
        row.put("value", value);
        if (serviceInstance != null && !serviceInstance.isBlank()) {
            row.put("service_instance", serviceInstance);
        }
        if (tagHost != null && !tagHost.isBlank()) {
            row.put("tag_host", tagHost);
        }
        putIfPresent(row, "threadPoolName", threadPoolName);
        putIfPresent(row, "objectPoolName", objectPoolName);
        putIfPresent(row, "httpConnectionPoolName", httpConnectionPoolName);
        putIfPresent(row, "connectionPoolName", connectionPoolName);
        putIfPresent(row, "poolName", poolName);
        return JSON.writeValueAsBytes(row);
    }

    private static void putIfPresent(Map<String, Object> row, String key, String value) {
        if (value != null && !value.isBlank()) {
            row.put(key, value);
        }
    }
}
