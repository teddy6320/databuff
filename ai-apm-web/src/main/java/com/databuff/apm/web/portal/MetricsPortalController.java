package com.databuff.apm.web.portal;

import com.databuff.apm.common.metric.MetricQueryDefinition;
import com.databuff.apm.web.config.common.CommonResponse;
import com.databuff.apm.web.metric.MetricCoreCatalogService;
import com.databuff.apm.web.metric.MetricQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Portal metrics catalog and query APIs ({@code /webapi/metrics/*}). */
@RestController
@RequestMapping("/metrics")
public class MetricsPortalController {

    private final MetricQueryService metricQueryService;
    private final MetricCoreCatalogService catalogService;

    public MetricsPortalController(MetricQueryService metricQueryService, MetricCoreCatalogService catalogService) {
        this.metricQueryService = metricQueryService;
        this.catalogService = catalogService;
    }

    @GetMapping("/getMetricTypes")
    public Map<String, Object> getMetricTypes(
            @RequestParam(value = "builtin", required = false) Boolean builtin,
            @RequestParam(value = "system", required = false) Boolean system) {
        return CommonResponse.ok(catalogService.findAllTypes(builtin != null ? builtin : system));
    }

    @PostMapping("/searchMetricTypes")
    public Map<String, Object> searchMetricTypes(@RequestBody(required = false) Map<String, Object> body) {
        String typeKey = body == null ? null : stringValue(body.get("typeKey"));
        String metricKey = body == null ? null : stringValue(body.get("metricKey"));
        return CommonResponse.ok(catalogService.searchMetricTypes(typeKey, metricKey));
    }

    @GetMapping("/findMetric")
    public Map<String, Object> findMetric(
            @RequestParam(required = false) String type1,
            @RequestParam(required = false) String type2,
            @RequestParam(required = false) String type3) {
        List<String> metrics = catalogService.findMetricByType(type1, type2, type3)
                .stream()
                .sorted()
                .collect(Collectors.toList());
        return CommonResponse.ok(metrics);
    }

    @PostMapping("/searchAllMetrics")
    public Map<String, Object> searchAllMetrics(@RequestBody(required = false) Map<String, Object> body) {
        return CommonResponse.ok(catalogService.searchAllMetrics(
                stringValue(body == null ? null : body.get("type1")),
                stringValue(body == null ? null : body.get("type2")),
                stringValue(body == null ? null : body.get("type3")),
                stringValue(body == null ? null : body.get("host")),
                stringValue(body == null ? null : body.get("app"))));
    }

    @GetMapping("/query/in")
    public Map<String, Object> queryIn(@RequestParam("metrics") Collection<String> metrics) {
        return CommonResponse.ok(catalogService.queryIn(metrics));
    }

    @GetMapping("/query/tagKey/all")
    public Map<String, Object> allTagKeys() {
        return CommonResponse.ok(catalogService.allTagKeys());
    }

    @PostMapping("/listTagValues")
    public Map<String, Object> listTagValues(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(resolveTagValues(body));
    }

    @PostMapping("/lastLastTagValues")
    public Map<String, Object> lastLastTagValues(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(resolveTagValues(body));
    }

    private Map<String, Set<String>> resolveTagValues(Map<String, Object> body) {
        long end = System.currentTimeMillis();
        long start = end - 86_400_000L;
        if (body.get("start") instanceof Number startNum) {
            start = normalizeTime(startNum.longValue());
        }
        if (body.get("end") instanceof Number endNum) {
            end = normalizeTime(endNum.longValue());
        }
        List<String> metrics = parseMetricsList(body);
        List<String> by = parseStringList(body.get("by"));
        if (by.isEmpty()) {
            by = resolveFilterTagKeys(metrics);
        }
        List<MetricQueryService.MetricFilter> filters = parseFilters(body.get("from"));
        MetricQueryService.LastTagsRequest request = new MetricQueryService.LastTagsRequest(
                start, end, metrics, by, filters);
        Map<String, List<String>> tags = metricQueryService.lastTags(request);
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : tags.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }

    private List<String> parseMetricsList(Map<String, Object> body) {
        List<String> metrics = parseStringList(body.get("metrics")).stream()
                .filter(metric -> metric != null && !metric.isBlank())
                .toList();
        if (!metrics.isEmpty()) {
            return metrics;
        }
        String metric = stringValue(body.get("metric"));
        if (!metric.isBlank()) {
            return List.of(metric);
        }
        return List.of();
    }

    private List<String> resolveFilterTagKeys(List<String> metrics) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (String metric : metrics) {
            MetricQueryDefinition definition = catalogService.findOpenByIdentifier(metric);
            if (definition == null || definition.getTagKey() == null) {
                continue;
            }
            keys.addAll(definition.getTagKey().keySet());
        }
        return new ArrayList<>(keys);
    }

    @PostMapping("/exploreMetricByGroupGraph")
    public Map<String, Object> exploreMetricByGroupGraph(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> series = metricQueryService.metricChart(normalizeChartBody(body));
        return CommonResponse.ok(series);
    }

    /**
     * Frontend {@code MetricApi.getMetricChart} flattens {@code query.A} to the request root;
     * restore the nested shape expected by {@link MetricQueryService#metricChart(Map)}.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeChartBody(Map<String, Object> body) {
        if (body.get("query") instanceof Map<?, ?>) {
            return body;
        }
        Map<String, Object> normalized = new LinkedHashMap<>(body);
        Object metric = body.get("metric");
        if (metric == null) {
            return normalized;
        }
        Map<String, Object> queryA = new LinkedHashMap<>();
        for (String key : List.of("metric", "from", "by", "aggs", "types", "order")) {
            if (body.containsKey(key)) {
                queryA.put(key, body.get(key));
            }
        }
        normalized.put("query", Map.of("A", queryA));
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private static List<MetricQueryService.MetricFilter> parseFilters(Object fromObject) {
        if (!(fromObject instanceof List<?> list)) {
            return List.of();
        }
        List<MetricQueryService.MetricFilter> filters = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> mapRaw) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) mapRaw;
                Object operator = map.get("operator");
                Object connector = map.get("connector");
                filters.add(new MetricQueryService.MetricFilter(
                        String.valueOf(map.get("left")),
                        String.valueOf(operator != null ? operator : "="),
                        map.get("right"),
                        String.valueOf(connector != null ? connector : "AND")));
            }
        }
        return filters;
    }

    private static List<String> parseStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static long normalizeTime(long value) {
        return value < 1_000_000_000_000L ? value * 1000L : value;
    }
}
