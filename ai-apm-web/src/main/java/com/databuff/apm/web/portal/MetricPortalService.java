package com.databuff.apm.web.portal;

import com.databuff.apm.common.query.ApmQueryModels;
import com.databuff.apm.common.query.ApmQueryModels.ServiceMetricPoint;
import com.databuff.apm.web.metric.MetricQueryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MetricPortalService {

    private final MetricQueryService metricQueryService;

    public MetricPortalService(MetricQueryService metricQueryService) {
        this.metricQueryService = metricQueryService;
    }

    public List<Map<String, Object>> chart(Map<String, Object> body) {
        return metricQueryService.metricChart(body);
    }

    public Map<String, List<String>> lastTags(Map<String, Object> body) {
        MetricQueryService.LastTagsRequest request = new MetricQueryService.LastTagsRequest(
                toLong(body.get("start")),
                toLong(body.get("end")),
                parseStringList(body.get("metrics")),
                parseStringList(body.get("by")),
                parseFilters(body.get("from")));
        return metricQueryService.lastTags(request);
    }

    public List<ServiceMetricPoint> serviceSeries(Map<String, Object> body) {
        String service = body.get("service") == null ? null : String.valueOf(body.get("service"));
        long from = normalizeTime(toLong(body.get("from")));
        long to = normalizeTime(toLong(body.get("to")));
        return metricQueryService.serviceSeries(new MetricQueryService.ServiceSeriesRequest(service, from, to));
    }

    public List<ApmQueryModels.HttpEndpointPoint> httpEndpoints(Map<String, Object> body) {
        return metricQueryService.httpEndpoints(parseHttpQuery(body));
    }

    public List<ApmQueryModels.HttpLatencyBucketPoint> httpLatency(
            Map<String, Object> body) {
        return metricQueryService.httpLatencyBuckets(parseHttpQuery(body));
    }

    public List<ApmQueryModels.MetricSeriesPoint> metricSeries(Map<String, Object> body) {
        return metricQueryService.metricSeries(parseMetricSeriesRequest(body));
    }

    private static MetricQueryService.HttpQueryRequest parseHttpQuery(Map<String, Object> body) {
        String service = body.get("service") == null ? null : String.valueOf(body.get("service"));
        long from = normalizeTime(toLong(body.get("from")));
        long to = normalizeTime(toLong(body.get("to")));
        int limit = (int) toLong(body.get("limit"));
        if (limit <= 0) {
            limit = 20;
        }
        String httpMethod = body.get("httpMethod") == null ? null : String.valueOf(body.get("httpMethod"));
        String httpCode = body.get("httpCode") == null ? null : String.valueOf(body.get("httpCode"));
        String urlContains = body.get("urlContains") == null ? null : String.valueOf(body.get("urlContains"));
        return new MetricQueryService.HttpQueryRequest(
                service, from, to, limit, httpMethod, httpCode, urlContains);
    }

    private static MetricQueryService.MetricSeriesRequest parseMetricSeriesRequest(Map<String, Object> body) {
        String metric = String.valueOf(body.getOrDefault("metric", ""));
        Object fromField = body.get("from");
        long start;
        if (body.get("start") != null) {
            start = toLong(body.get("start"));
        } else if (fromField instanceof Number || isNumericString(fromField)) {
            start = toLong(fromField);
        } else {
            start = 0L;
        }
        long end = toLong(body.get("end") != null ? body.get("end") : body.get("to"));
        List<MetricQueryService.MetricFilter> filters = fromField instanceof List<?> list
                ? parseFilters(list)
                : List.of();
        return new MetricQueryService.MetricSeriesRequest(metric, start, end, filters);
    }

    private static boolean isNumericString(Object value) {
        if (!(value instanceof String text) || text.isBlank()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static long normalizeTime(long value) {
        if (value <= 0) {
            return System.currentTimeMillis();
        }
        return value < 1_000_000_000_000L ? value * 1000L : value;
    }

    @SuppressWarnings("unchecked")
    private static List<String> parseStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }

    @SuppressWarnings("unchecked")
    private static List<MetricQueryService.MetricFilter> parseFilters(Object fromObject) {
        if (!(fromObject instanceof List<?> list)) {
            return List.of();
        }
        List<MetricQueryService.MetricFilter> filters = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
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
}
