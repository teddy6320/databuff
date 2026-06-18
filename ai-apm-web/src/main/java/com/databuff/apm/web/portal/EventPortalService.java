package com.databuff.apm.web.portal;

import com.databuff.apm.common.util.PortalServiceIdResolver;
import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.EventRuleService;
import com.databuff.apm.web.monitor.service.AlarmService;
import com.databuff.apm.web.persistence.EventPersistence;
import com.databuff.apm.web.config.common.CommonResponse;
import com.databuff.apm.web.metric.MetricQueryService;
import com.databuff.apm.web.monitor.policy.AlarmExportSupport;
import com.databuff.apm.web.monitor.policy.AlarmPolicySupport;
import com.databuff.apm.web.monitor.policy.ResponsePolicyService;
import com.databuff.apm.common.storage.ApmConfigRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventPortalService {

    private final EventRuleService eventRuleService;
    private final AlarmService alarmService;
    private final EventPersistence eventPersistence;
    private final ResponsePolicyService responsePolicyService;
    private final MetricQueryService metricQueryService;
    private final MetricPortalService metricPortalService;

    public EventPortalService(
            EventRuleService eventRuleService,
            AlarmService alarmService,
            EventPersistence eventPersistence,
            ResponsePolicyService responsePolicyService,
            MetricQueryService metricQueryService,
            MetricPortalService metricPortalService) {
        this.eventRuleService = eventRuleService;
        this.alarmService = alarmService;
        this.eventPersistence = eventPersistence;
        this.responsePolicyService = responsePolicyService;
        this.metricQueryService = metricQueryService;
        this.metricPortalService = metricPortalService;
    }

    public Map<String, Object> searchRules(Map<String, Object> body) {
        Map<String, Object> params = body == null ? Map.of() : body;
        String keyword = AlarmPolicySupport.stringValue(params.get("ruleName"), "");
        Object enabled = params.get("enabled");
        List<Map<String, Object>> all = eventRuleService.listRules().stream()
                .map(this::toPortalRule)
                .toList();
        long total = AlarmPolicySupport.count(all, row ->
                AlarmPolicySupport.keywordMatch(row, keyword, "ruleName", "name")
                        && AlarmPolicySupport.enabledMatch(row, enabled));
        List<Map<String, Object>> page = AlarmPolicySupport.filterSortPage(
                all,
                params,
                row -> AlarmPolicySupport.keywordMatch(row, keyword, "ruleName", "name")
                        && AlarmPolicySupport.enabledMatch(row, enabled),
                "updateTime");
        return CommonResponse.listData(page, total);
    }

    public Map<String, Object> searchSystemRules(Map<String, Object> body) {
        return searchRules(body);
    }

    public Map<String, Object> findMonitorEventV2(Map<String, Object> body) {
        String alarmId = stringValue(body.get("alarmId"), null);
        Optional<Alarm> alarm = alarmService.findEvent(alarmId);
        if (alarm.isEmpty()) {
            return CommonResponse.listData(List.of(), 0);
        }
        if (eventPersistence == null || !eventPersistence.isPersistenceEnabled()) {
            return CommonResponse.listData(List.of(), 0);
        }
        List<Map<String, Object>> rows = eventPersistence.listForAlarm(alarm.get()).stream()
                .map(this::toPortalEvent)
                .toList();
        return CommonResponse.listData(rows, rows.size());
    }

    public Map<String, Object> findEventDetailV2(Map<String, Object> body) {
        String eventId = stringValue(body.get("eventId"), null);
        if (eventId == null) {
            eventId = stringValue(body.get("id"), null);
        }
        if (eventId == null || eventPersistence == null || !eventPersistence.isPersistenceEnabled()) {
            return CommonResponse.fail(404, "事件不存在");
        }
        Optional<ApmConfigRepository.EventRow> event = eventPersistence.findById(eventId);
        if (event.isEmpty()) {
            return CommonResponse.fail(404, "事件不存在");
        }
        return CommonResponse.ok(toPortalEventDetail(event.get()));
    }

    public Map<String, Object> monitorObjs() {
        Map<String, Object> service = new LinkedHashMap<>();
        service.put("code", "service");
        service.put("name", "服务");
        return CommonResponse.ok(List.of(service));
    }

    public Map<String, Object> previewMonitorGraphV3(Map<String, Object> body) {
        Map<String, Object> request = body == null ? Map.of() : body;
        List<Map<String, Object>> series = metricPortalService.chart(request);
        return CommonResponse.ok(series);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getEventChartMap(Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (body == null || body.isEmpty()) {
            return CommonResponse.ok(data);
        }
        long start = longValue(body.get("start"), 0);
        long end = longValue(body.get("end"), 0);
        int interval = (int) longValue(body.get("interval"), 60);
        if (start <= 0 || end <= 0) {
            return CommonResponse.ok(data);
        }

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            String key = entry.getKey();
            if (key.isEmpty() || !key.chars().allMatch(Character::isDigit)) {
                continue;
            }
            if (!(entry.getValue() instanceof Map<?, ?> rawItem)) {
                continue;
            }
            Map<String, Object> queryItem = (Map<String, Object>) rawItem;
            String metric = extractQueryMetric(queryItem);
            if (metric == null || metric.isBlank()) {
                continue;
            }

            Map<String, Object> chartRequest = new LinkedHashMap<>();
            chartRequest.put("start", start);
            chartRequest.put("end", end);
            chartRequest.put("interval", interval);
            Object metricBlock = queryItem.get("A");
            if (!(metricBlock instanceof Map<?, ?>)) {
                continue;
            }
            chartRequest.put("query", Map.of("A", metricBlock));

            List<Map<String, Object>> seriesList = metricPortalService.chart(chartRequest);
            if (seriesList.isEmpty()) {
                continue;
            }
            List<Map<String, Object>> enriched = new ArrayList<>();
            for (Map<String, Object> series : seriesList) {
                enriched.add(enrichEventChartSeries(series, queryItem, metric));
            }
            data.put(key + ":" + metric, enriched);
        }
        return CommonResponse.ok(data);
    }

    public Map<String, Object> batchDeleteRules(List<?> body) {
        for (Long id : extractRuleIds(body)) {
            eventRuleService.deleteRule(id);
        }
        return CommonResponse.ok(null);
    }

    public EventRule createRule(Map<String, Object> body) {
        return eventRuleService.createRuleFromBody(body);
    }

    public Map<String, Object> editMonitor(Map<String, Object> body) {
        long id = longValue(body.get("id"), -1);
        Optional<EventRule> existing = eventRuleService.findRule(id);
        if (existing.isEmpty()) {
            return CommonResponse.fail(404, "规则不存在");
        }
        EventRule updated = eventRuleService.saveRule(existing.get(), body);
        return CommonResponse.ok(toPortalRule(updated));
    }

    public Map<String, Object> getMonitorDetail(Map<String, Object> body) {
        long id = longValue(body.get("id"), longValue(body.get("monitorId"), -1));
        return eventRuleService.findRule(id)
                .map(rule -> CommonResponse.ok(toPortalRuleDetail(rule)))
                .orElseGet(() -> CommonResponse.fail(404, "规则不存在"));
    }

    public Map<String, Object> toggleRulesEnabled(List<Long> ids, boolean enabled) {
        for (Long id : ids) {
            eventRuleService.updateEnabled(id, enabled);
        }
        return CommonResponse.ok(null);
    }

    public Map<String, Object> searchResponsePolicies(Map<String, Object> body) {
        Map<String, Object> page = responsePolicyService.list(body);
        return CommonResponse.ok(page);
    }

    public Map<String, Object> findResponsePolicy(Map<String, Object> body) {
        int id = AlarmPolicySupport.intValue(body.get("id"), -1);
        return responsePolicyService.findById(id)
                .map(CommonResponse::ok)
                .orElseGet(() -> CommonResponse.fail(404, "响应策略不存在"));
    }

    public Map<String, Object> saveResponsePolicy(Map<String, Object> body) {
        return responsePolicyService.save(body);
    }

    public void deleteResponsePolicies(Map<String, Object> body) {
        responsePolicyService.delete(AlarmPolicySupport.intList(body.get("ids")));
    }

    public void publishResponsePolicies(Map<String, Object> body) {
        responsePolicyService.publish(
                AlarmPolicySupport.intList(body.get("ids")),
                AlarmPolicySupport.boolValue(body.get("enabled"), true));
    }

    public void exportResponsePolicies(Map<String, Object> body, HttpServletResponse response) throws IOException {
        AlarmExportSupport.writeCsv(
                response,
                "response-policies.csv",
                List.of("策略名称", "启停状态", "更新时间"),
                responsePolicyService.export(body),
                List.of("policyName", "enabled", "updatedTime"));
    }

    private Map<String, Object> toPortalRule(EventRule rule) {
        long updatedAt = rule.updatedAt() == null ? AlarmPolicySupport.nowMillis() : rule.updatedAt().toEpochMilli();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rule.id());
        row.put("ruleName", rule.ruleName());
        row.put("name", rule.ruleName());
        row.put("enabled", rule.enabled());
        row.put("service", rule.service());
        row.put("classification", rule.classify());
        row.put("type", rule.detectionWay());
        row.put("detectionWay", rule.detectionWay());
        row.put("classify", rule.classify());
        row.put("updateTime", updatedAt);
        row.put("createTime", updatedAt);
        row.put("editor", "admin");
        row.put("creator", "admin");
        return row;
    }

    private Map<String, Object> toPortalRuleDetail(EventRule rule) {
        Map<String, Object> row = toPortalRule(rule);
        row.put("threshold", rule.threshold());
        row.put("metric", rule.metric());
        row.put("comparator", rule.comparator());
        if (rule.queryJson() != null && !rule.queryJson().isBlank()) {
            row.put("query", parseQueryObject(rule.queryJson()));
        } else {
            row.put("query", defaultQuery(rule));
        }
        return row;
    }

    private static Object parseQueryObject(String queryJson) {
        if (queryJson == null || queryJson.isBlank()) {
            return Map.of();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(queryJson, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static Map<String, Object> defaultQuery(EventRule rule) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("metric", rule.metric());
        metric.put("aggs", "avg");
        metric.put("by", List.of());
        metric.put("from", List.of(Map.of("type", "service", "value", rule.service())));

        Map<String, Object> thresholds = new LinkedHashMap<>();
        thresholds.put("critical", Map.of(
                "value", rule.threshold(),
                "comparator", rule.comparator()));

        Map<String, Object> queryItem = new LinkedHashMap<>();
        queryItem.put("way", rule.detectionWay());
        queryItem.put("A", metric);
        queryItem.put("thresholds", thresholds);
        queryItem.put("comparison", rule.comparator());
        return Map.of("1", queryItem);
    }

    @SuppressWarnings("unchecked")
    private static String extractQueryMetric(Map<String, Object> queryItem) {
        Object metricObj = queryItem.get("A");
        if (metricObj instanceof Map<?, ?> metric) {
            return stringValue(metric.get("metric"), null);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> enrichEventChartSeries(
            Map<String, Object> series,
            Map<String, Object> queryItem,
            String metric) {
        Double critical = extractThresholdLevel(queryItem, "critical");
        Double warning = extractThresholdLevel(queryItem, "warning");

        List<String> columns = new ArrayList<>();
        columns.add("time");
        columns.add(metric);
        if (critical != null) {
            columns.add("critical");
        }
        if (warning != null) {
            columns.add("warning");
        }

        List<List<Number>> values = series.get("values") instanceof List<?> rawValues
                ? (List<List<Number>>) rawValues
                : List.of();
        List<List<Number>> enrichedValues = new ArrayList<>();
        for (List<Number> row : values) {
            List<Number> enriched = new ArrayList<>(row);
            if (critical != null) {
                enriched.add(critical);
            }
            if (warning != null) {
                enriched.add(warning);
            }
            enrichedValues.add(enriched);
        }

        Map<String, Object> enriched = new LinkedHashMap<>(series);
        enriched.put("columns", columns);
        enriched.put("values", enrichedValues);
        return enriched;
    }

    private static Double extractThresholdLevel(Map<String, Object> queryItem, String level) {
        Object thresholdsObj = queryItem.get("thresholds");
        if (!(thresholdsObj instanceof Map<?, ?> thresholds)) {
            return null;
        }
        Object levelObj = thresholds.get(level);
        if (levelObj instanceof Number number) {
            return number.doubleValue();
        }
        if (levelObj instanceof Map<?, ?> levelMap) {
            Object value = levelMap.get("value");
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            if (value != null) {
                try {
                    return Double.parseDouble(String.valueOf(value).trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }
        if (levelObj == null) {
            return null;
        }
        String text = String.valueOf(levelObj).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static List<Long> extractRuleIds(Object body) {
        if (body instanceof List<?> list) {
            List<Long> ids = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    ids.add(longValue(map.get("id"), -1));
                } else {
                    ids.add(longValue(item, -1));
                }
            }
            return ids.stream().filter(id -> id > 0).toList();
        }
        if (body instanceof Map<?, ?> map) {
            return longList(map.get("ids"));
        }
        return List.of();
    }

    private Map<String, Object> toPortalEvent(ApmConfigRepository.EventRow event) {
        long start = event.triggeredAt().toEpochMilli();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", event.id());
        row.put("message", event.message());
        row.put("startTriggerTime", start);
        row.put("triggerTime", start);
        row.put("level", portalLevel(event.level()));
        row.put("serviceId", PortalServiceIdResolver.normalize(event.service()));
        enrichPortalEventFields(row, event);
        return row;
    }

    private Map<String, Object> toPortalEventDetail(ApmConfigRepository.EventRow event) {
        Map<String, Object> row = toPortalEvent(event);
        row.put("detectionWay", event.detectionWay());
        return row;
    }

    private void enrichPortalEventFields(Map<String, Object> row, ApmConfigRepository.EventRow event) {
        row.put("ruleName", event.ruleName());
        row.put("classification", eventRuleService.findRule(event.ruleId())
                .map(EventRule::classify)
                .orElse("singleMetric"));
        Map<String, Object> trigger = new LinkedHashMap<>();
        String serviceId = PortalServiceIdResolver.normalize(event.service());
        trigger.put("service", List.of(event.service()));
        trigger.put("serviceId", List.of(serviceId));
        row.put("trigger", trigger);
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.put("service", List.of(event.service()));
        tags.put("ruleName", List.of(event.ruleName()));
        if (event.groupKey() != null && !event.groupKey().isBlank()) {
            tags.put("group", List.of(event.groupKey()));
        }
        row.put("tags", tags);
        eventRuleService.findRule(event.ruleId()).ifPresent(rule -> {
            if (rule.metric() != null && !rule.metric().isBlank()) {
                row.put("metrics", List.of(rule.metric()));
            }
            if (rule.queryJson() != null && !rule.queryJson().isBlank()) {
                row.put("query", parseQueryObject(rule.queryJson()));
            } else {
                row.put("query", defaultQuery(rule));
            }
        });
    }

    private static int portalLevel(String level) {
        return "critical".equalsIgnoreCase(level) || "error".equalsIgnoreCase(level) ? 3 : 2;
    }

    private static List<Long> longList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(item -> longValue(item, -1)).filter(id -> id > 0).toList();
    }

    private static String stringValue(Object primary, String fallback) {
        String value = primary == null ? null : String.valueOf(primary).trim();
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return fallback;
    }

    private static double doubleValue(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long longValue(Object value, long defaultValue) {
        return AlarmPolicySupport.longValue(value, defaultValue);
    }
}
