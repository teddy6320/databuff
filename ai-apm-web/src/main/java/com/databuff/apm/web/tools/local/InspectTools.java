package com.databuff.apm.web.tools.local;

import com.databuff.apm.common.time.ApmTimeZones;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.MetricQueryBuilder;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.portal.ServicePortalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Lazy
public class InspectTools {

    @Autowired
    private ServicePortalService servicePortalService;
    @Autowired
    private ApmReadRepository readRepository;
    @Autowired
    private ApmStorageProperties storageProperties;
    @Autowired
    private ObjectMapper objectMapper;
    private String metricDatabase;

    @PostConstruct
    void initMetricDatabase() {
        metricDatabase = storageProperties == null ? "databuff" : storageProperties.metricDatabase();
    }

    @Tool(converter = PlainTextToolResultConverter.class, description = "Inspect one service by serviceName. No time range input is required. It queries entry service metrics, runs threshold-free anomaly detection, and for web services also checks exception and JVM GC signals.")
    public String inspectService(
            @ToolParam(name = "serviceName", description = "Service name to inspect")
            String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return json(Map.of("ok", false, "message", "serviceName is required"));
        }
        String service = serviceName.trim();
        String from = ApmTimeZones.WALL_CLOCK.format(Instant.ofEpochMilli(System.currentTimeMillis() - 3_600_000L));
        String to = ApmTimeZones.WALL_CLOCK.format(Instant.now());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("serviceId", service);
        body.put("fromTime", from);
        body.put("toTime", to);
        body.put("interval", 60);
        body.put("size", 50);

        Map<String, Object> serviceInfo = servicePortalService.serviceInfo(body);
        String serviceType = stringValue(serviceInfo == null ? null : serviceInfo.get("service_type"), "");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("serviceName", service);
        result.put("serviceInfo", serviceInfo == null ? Map.of() : serviceInfo);
        result.put("fromTime", from);
        result.put("toTime", to);
        result.put("entryMetrics", inspectEntryMetrics(body));

        if (isWebService(serviceType, serviceInfo)) {
            result.put("exceptionMetrics", inspectExceptionMetrics(body));
            result.put("jvmMetrics", inspectJvmMetrics(service, from, to));
        }
        result.put("summary", summarize(result));
        return json(result);
    }

    private Map<String, Object> inspectEntryMetrics(Map<String, Object> baseBody) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reqCount", inspectTrendMetric(baseBody, "reqCount", "入口请求量"));
        result.put("errRate", inspectTrendMetric(baseBody, "errRate", "入口错误率"));
        result.put("avgTime", inspectTrendMetric(baseBody, "avgTime", "入口平均响应时间"));
        return result;
    }

    private Map<String, Object> inspectTrendMetric(Map<String, Object> baseBody, String metric, String label) {
        Map<String, Object> body = new LinkedHashMap<>(baseBody);
        body.put("metric", metric);
        List<Map<String, Object>> series = servicePortalService.serviceDetailTrendChart(body);
        List<Double> values = extractSeriesValues(series);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metric", metric);
        result.put("label", label);
        result.put("data", series);
        result.put("detection", detectAnomaly(values));
        return result;
    }

    private Map<String, Object> inspectExceptionMetrics(Map<String, Object> baseBody) {
        Map<String, Object> body = new LinkedHashMap<>(baseBody);
        body.put("groupBy", "exceptionName");
        body.put("size", 10);
        Map<String, Object> data = servicePortalService.exceptionDistMap(body);
        List<Double> values = new ArrayList<>();
        Object list = data == null ? null : data.get("list");
        if (list instanceof Iterable<?> rows) {
            for (Object row : rows) {
                if (row instanceof Map<?, ?> map) {
                    values.add(numberValue(map.get("errCnt")));
                }
            }
        }
        return Map.of(
                "label", "服务异常分布",
                "data", data == null ? Map.of() : data,
                "detection", detectAnomaly(values));
    }

    private Map<String, Object> inspectJvmMetrics(String serviceName, String fromTime, String toTime) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (readRepository == null) {
            result.put("available", false);
            result.put("message", "readRepository is not ready");
            return result;
        }
        long fromMillis = ApmTimeZones.wallClockToEpochMilli(fromTime);
        long toMillis = ApmTimeZones.wallClockToEpochMilli(toTime);
        result.put("gcMajorCount", inspectJvmSqlMetric(
                serviceName, fromMillis, toMillis, "metric_jvm", "gc_major_collection_count", "JVM Major GC 次数"));
        result.put("gcMajorTime", inspectJvmSqlMetric(
                serviceName, fromMillis, toMillis, "metric_jvm", "gc_major_collection_time", "JVM Major GC 耗时"));
        result.put("threadCount", inspectJvmSqlMetric(
                serviceName, fromMillis, toMillis, "metric_jvm", "thread_count", "JVM 线程数"));
        return result;
    }

    private Map<String, Object> inspectJvmSqlMetric(
            String serviceName,
            long fromMillis,
            long toMillis,
            String table,
            String field,
            String label) {
        String filter = " AND `service` = '" + escapeLiteral(serviceName) + "'";
        String sql = MetricQueryBuilder.isJvmGcMonotonicField(field)
                ? MetricQueryBuilder.metricFieldSeriesSql(
                        metricDatabase, table, field, fromMillis, toMillis, filter, 60, "sum")
                : """
                SELECT CAST(FLOOR(`ts` / 60000) * 60 AS BIGINT) AS epoch_sec, SUM(`%s`) AS metric_value
                FROM %s.`%s`
                WHERE `ts` >= %d AND `ts` < %d AND `service` = '%s'
                GROUP BY epoch_sec
                ORDER BY epoch_sec ASC
                LIMIT 120
                """.formatted(field, metricDatabase, table, fromMillis, toMillis, escapeLiteral(serviceName));
        try {
            List<Map<String, Object>> rows = MetricToolResultFormat.formatEpochSecRows(readRepository.queryRows(sql, 120));
            List<Double> values = rows.stream()
                    .map(row -> numberValue(row.get("metric_value")))
                    .toList();
            return Map.of("label", label, "data", rows, "detection", detectAnomaly(values));
        } catch (Exception e) {
            return Map.of("label", label, "data", List.of(), "detection", detectAnomaly(List.of()),
                    "error", e.getMessage() == null ? "query failed" : e.getMessage());
        }
    }

    public static Map<String, Object> detectAnomaly(List<Double> values) {
        List<Double> normalized = values == null ? List.of() : values.stream()
                .filter(value -> value != null && Double.isFinite(value))
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("points", normalized.size());
        if (normalized.size() < 3) {
            result.put("anomaly", false);
            result.put("reason", "数据点不足");
            return result;
        }
        double mean = normalized.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = normalized.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0);
        double std = Math.sqrt(variance);
        double latest = normalized.get(normalized.size() - 1);
        double max = normalized.stream().mapToDouble(Double::doubleValue).max().orElse(latest);
        boolean spike = std > 0 && latest > mean + 3 * std;
        boolean jump = mean > 0 && latest > mean * 2 && latest == max;
        boolean anomaly = spike || jump;
        result.put("anomaly", anomaly);
        result.put("latest", latest);
        result.put("mean", mean);
        result.put("std", std);
        result.put("max", max);
        result.put("reason", anomaly ? "最新点相对历史均值存在明显突增" : "未发现明显突增");
        return result;
    }

    private List<Double> extractSeriesValues(List<Map<String, Object>> series) {
        if (series == null || series.isEmpty()) {
            return List.of();
        }
        List<Double> values = new ArrayList<>();
        for (Map<String, Object> item : series) {
            Object rawValues = item.get("values");
            if (!(rawValues instanceof Iterable<?> points)) {
                continue;
            }
            for (Object point : points) {
                if (point instanceof List<?> tuple && tuple.size() >= 2) {
                    values.add(numberValue(tuple.get(1)));
                }
            }
        }
        return values;
    }

    private static String summarize(Map<String, Object> result) {
        List<String> abnormal = new ArrayList<>();
        collectAbnormalLabels(result.get("entryMetrics"), abnormal);
        collectAbnormalLabels(result.get("exceptionMetrics"), abnormal);
        collectAbnormalLabels(result.get("jvmMetrics"), abnormal);
        if (abnormal.isEmpty()) {
            return "初步巡检未发现明显异常。";
        }
        return "初步巡检发现可疑异常：" + String.join("、", abnormal);
    }

    private static void collectAbnormalLabels(Object node, List<String> labels) {
        if (node instanceof Map<?, ?> map) {
            Object detection = map.get("detection");
            if (detection instanceof Map<?, ?> d && Boolean.TRUE.equals(d.get("anomaly"))) {
                Object label = map.get("label");
                labels.add(label == null ? "未知指标" : String.valueOf(label));
            }
            for (Object value : map.values()) {
                collectAbnormalLabels(value, labels);
            }
        }
    }

    private static boolean isWebService(String serviceType, Map<String, Object> serviceInfo) {
        if ("web".equalsIgnoreCase(serviceType) || "service".equalsIgnoreCase(serviceType)) {
            return true;
        }
        Object type = serviceInfo == null ? null : serviceInfo.get("type");
        return type != null && "web".equalsIgnoreCase(String.valueOf(type));
    }

    private static double numberValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String stringValue(Object value, String fallback) {
        String text = value == null ? "" : String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    private static String escapeLiteral(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
