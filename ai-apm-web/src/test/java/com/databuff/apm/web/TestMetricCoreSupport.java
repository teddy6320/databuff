package com.databuff.apm.web;

import com.databuff.apm.common.metric.MetricCoreSeedRow;
import com.databuff.apm.web.metric.MetricCoreCatalogService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal metric catalog fixtures for unit/integration tests. */
public final class TestMetricCoreSupport {

    private TestMetricCoreSupport() {
    }

    public static MetricCoreCatalogService catalogWithServiceMetrics() {
        MetricCoreCatalogService service = new MetricCoreCatalogService();
        service.reloadFromRows(List.of(serviceSeedRow()));
        return service;
    }

    private static MetricCoreSeedRow serviceSeedRow() {
        Map<String, Map<String, Object>> fields = new LinkedHashMap<>();
        fields.put("cnt", field("请求次数", "sum", "SUM"));
        fields.put("error", field("错误次数", "sum", "SUM"));
        fields.put("sumDuration", field("总耗时", "sum", "SUM"));
        fields.put("maxDuration", field("最大耗时", "max", "MAX"));
        fields.put("slowCnt", field("慢请求次数", "sum", "SUM"));
        fields.put("verySlowCnt", field("极慢请求次数", "sum", "SUM"));
        fields.put("apdex", field("Apdex", "avg", "GAUGE"));
        fields.put("healthStatus", field("健康状态", "avg", "GAUGE"));
        return new MetricCoreSeedRow(
                10L,
                "应用性能",
                "入口请求",
                "请求总览",
                "apm",
                "databuff",
                "service",
                "metric_service",
                "【服务入口指标】服务请求次数",
                Map.of(
                        "service", "服务名称",
                        "serviceId", "服务Id",
                        "serviceInstance", "服务实例",
                        "errorType", "错误类型"),
                Map.of(),
                fields,
                true,
                true);
    }

    private static Map<String, Object> field(String name, String aggregatorType, String metricModel) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("metric_cn", name);
        meta.put("describe", name);
        meta.put("aggregatorType", aggregatorType);
        meta.put("metric_model", metricModel);
        return meta;
    }
}
