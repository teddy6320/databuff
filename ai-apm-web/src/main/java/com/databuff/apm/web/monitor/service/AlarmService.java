package com.databuff.apm.web.monitor.service;

import com.databuff.apm.common.util.PortalServiceIdResolver;
import com.databuff.apm.web.portal.PortalTimeParser;
import com.databuff.apm.web.config.common.CommonResponse;
import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.AlarmStore;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.EventRuleService;
import com.databuff.apm.web.monitor.policy.AlarmPolicySupport;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlarmService {

    private static final long EVENT_COUNT = 1L;

    private final AlarmStore alarmStore;
    private final EventRuleService eventRuleService;

    public AlarmService(AlarmStore alarmStore, EventRuleService eventRuleService) {
        this.alarmStore = alarmStore;
        this.eventRuleService = eventRuleService;
    }

    public long countAlarms(Map<String, Object> body) {
        return filterEvents(body).size();
    }

    public Map<String, Object> queryParams(Map<String, Object> body) {
        List<Alarm> alarms = listAlarmsInQueryRange(body);
        Map<String, String> serviceNameById = new LinkedHashMap<>();
        Set<String> ruleNames = new LinkedHashSet<>();
        for (Alarm event : alarms) {
            String serviceName = event.service();
            if (serviceName != null && !serviceName.isBlank()) {
                String serviceId = PortalServiceIdResolver.normalize(serviceName);
                if (!serviceId.isBlank()) {
                    serviceNameById.putIfAbsent(serviceId, serviceName);
                }
            }
            resolveRuleNames(event).forEach(ruleNames::add);
        }
        List<Map<String, String>> serviceOptions = serviceNameById.entrySet().stream()
                .map(entry -> Map.of(
                        "serviceId", entry.getKey(),
                        "serviceName", entry.getValue()))
                .toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", serviceOptions);
        data.put("ruleName", List.copyOf(ruleNames));
        return CommonResponse.ok(data);
    }

    public Map<String, Object> trend(Map<String, Object> body) {
        long now = System.currentTimeMillis();
        long from = PortalTimeParser.rangeFrom(body, now - 24 * 3_600_000L);
        long to = PortalTimeParser.rangeTo(body, now + 60_000L);
        long intervalMs = parseIntervalMillis(body.get("interval"));

        List<Alarm> events = filterAlarms(body, listAlarmsInQueryRange(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to)));
        Map<Long, Map<String, Long>> buckets = initializeTrendBuckets(from, to, intervalMs);
        for (Alarm event : events) {
            incrementTrendBuckets(buckets, event, from, to, intervalMs);
        }

        Map<String, Object> trendData = new LinkedHashMap<>();
        for (Map.Entry<Long, Map<String, Long>> entry : buckets.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            Map<String, Long> counts = entry.getValue();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("count", counts.get("count"));
            trendData.put(String.valueOf(entry.getKey()), point);
        }
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("data", trendData);
        return CommonResponse.ok(wrapper);
    }

    public Map<String, Object> list(Map<String, Object> body) {
        Map<String, Object> params = body == null ? Map.of() : body;
        List<Alarm> alarms = listAlarmsInQueryRange(params);
        List<Map<String, Object>> rows = new ArrayList<>(filterAlarms(params, alarms).stream()
                .map(this::toPortalAlarm)
                .toList());
        rows.sort(alarmRowComparator(params));
        long total = rows.size();
        List<Map<String, Object>> page = paginateRows(rows, params);
        return CommonResponse.listData(page, total);
    }

    public Map<String, Object> detail(String id) {
        Optional<Alarm> event = findEvent(id);
        if (event.isEmpty()) {
            return CommonResponse.fail(404, "告警不存在");
        }
        return CommonResponse.ok(toPortalAlarmDetail(event.get()));
    }

    public Map<String, Object> eventTrend(String alarmId, String interval) {
        Optional<Alarm> alarm = findEvent(alarmId);
        if (alarm.isEmpty()) {
            return CommonResponse.ok(List.of());
        }
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("time", alarm.get().triggeredAt().toEpochMilli());
        point.put("eventCnt", EVENT_COUNT);
        return CommonResponse.ok(List.of(point));
    }

    private static long parseIntervalMillis(Object interval) {
        if (interval == null) {
            return 60_000L;
        }
        if (interval instanceof Number number) {
            return Math.max(1L, number.longValue()) * 1000L;
        }
        String text = String.valueOf(interval).trim();
        if (text.isEmpty()) {
            return 60_000L;
        }
        try {
            return Math.max(1L, Long.parseLong(text)) * 1000L;
        } catch (NumberFormatException e) {
            return 60_000L;
        }
    }

    private static Map<Long, Map<String, Long>> initializeTrendBuckets(long from, long to, long intervalMs) {
        Map<Long, Map<String, Long>> buckets = new LinkedHashMap<>();
        if (to <= from || intervalMs <= 0L) {
            return buckets;
        }
        long start = from / intervalMs * intervalMs;
        for (long time = start; time < to; time += intervalMs) {
            buckets.put(time, newTrendBucket());
        }
        return buckets;
    }

    private static void incrementTrendBuckets(
            Map<Long, Map<String, Long>> buckets,
            Alarm event,
            long queryFrom,
            long queryTo,
            long intervalMs) {
        long triggerMillis = event.triggeredAt().toEpochMilli();
        if (triggerMillis < queryFrom || triggerMillis >= queryTo) {
            return;
        }
        long bucket = triggerMillis / intervalMs * intervalMs;
        Map<String, Long> bucketData = buckets.computeIfAbsent(bucket, key -> newTrendBucket());
        incrementTrendBucket(bucketData);
    }

    private static Map<String, Long> newTrendBucket() {
        Map<String, Long> bucket = new LinkedHashMap<>();
        bucket.put("count", 0L);
        return bucket;
    }

    private static void incrementTrendBucket(Map<String, Long> bucket) {
        bucket.merge("count", 1L, Long::sum);
    }

    public Optional<Alarm> findEvent(String id) {
        return alarmStore.findById(id);
    }

    public List<Alarm> filterEvents(Map<String, Object> body) {
        return filterAlarms(body, listAlarmsInQueryRange(body));
    }

    private List<Alarm> listAlarmsInQueryRange(Map<String, Object> body) {
        long now = System.currentTimeMillis();
        long from = PortalTimeParser.rangeFrom(body, now - 24 * 3_600_000L);
        long to = PortalTimeParser.rangeTo(body, now + 60_000L);
        return listAlarmsInQueryRange(Instant.ofEpochMilli(from), Instant.ofEpochMilli(to));
    }

    private List<Alarm> listAlarmsInQueryRange(Instant fromInstant, Instant toInstant) {
        return alarmStore.listInTimeRange(fromInstant, toInstant);
    }

    private List<Alarm> filterAlarms(Map<String, Object> body, List<Alarm> alarms) {
        List<Integer> levels = parseStatuses(body.get("level"));
        String description = stringValue(body.get("description"));
        String idLike = stringValue(body.get("idLike"));
        List<String> serviceFilters = resolveTriggerValues(body, "serviceId");
        List<String> ruleNameFilters = resolveTriggerValues(body, "ruleName");
        return alarms.stream()
                .filter(event -> matchesLevel(event, levels))
                .filter(event -> matchesDescription(event, description))
                .filter(event -> matchesIdLike(event, idLike))
                .filter(event -> matchesService(event, serviceFilters))
                .filter(event -> matchesRuleName(resolveRuleNames(event), ruleNameFilters))
                .toList();
    }

    private Map<String, Object> toPortalAlarm(Alarm event) {
        AlarmTiming timing = resolveAlarmTiming(event);
        List<String> ruleNameList = resolveRuleNames(event);
        Map<String, Object> trigger = new LinkedHashMap<>();
        trigger.put("service", List.of(event.service()));
        trigger.put("serviceId", List.of(PortalServiceIdResolver.normalize(event.service())));
        if (!ruleNameList.isEmpty()) {
            trigger.put("ruleName", ruleNameList);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", event.id());
        row.put("description", event.message());
        row.put("startTriggerTime", timing.startMillis());
        row.put("timestamp", timing.startMillis());
        row.put("endTriggerTime", timing.endMillis());
        row.put("duration", timing.durationSeconds());
        row.put("eventCnt", EVENT_COUNT);
        row.put("level", portalLevel(event.level()));
        row.put("type", resolveAlarmType(event));
        row.put("trigger", trigger);
        row.put("tags", buildAlarmTags(event, ruleNameList));
        row.put("serviceId", PortalServiceIdResolver.normalize(event.service()));
        row.put("serviceName", event.service());
        row.put("triggerObject", event.service());
        row.put("abnormalMetrics", resolveAbnormalMetrics(event));
        if (!ruleNameList.isEmpty()) {
            row.put("ruleName", ruleNameList.get(0));
        }
        return row;
    }

    private Map<String, Object> toPortalAlarmDetail(Alarm event) {
        Map<String, Object> detail = toPortalAlarm(event);
        detail.put("eventId", List.of());
        detail.put("remark", List.of());
        return detail;
    }

    private static String resolveAlarmType(Alarm event) {
        return "convergence";
    }

    private static Map<String, Object> buildAlarmTags(Alarm alarm, List<String> ruleNames) {
        Map<String, Object> tags = new LinkedHashMap<>();
        if (alarm.service() != null && !alarm.service().isBlank()) {
            tags.put("service", List.of(alarm.service()));
        }
        if (!ruleNames.isEmpty()) {
            tags.put("ruleName", ruleNames);
        }
        return tags;
    }

    private List<String> resolveRuleNames(Alarm alarm) {
        if (alarm.service() == null || alarm.service().isBlank()) {
            return List.of();
        }
        return eventRuleService.listRules().stream()
                .filter(rule -> alarm.service().equals(rule.service()))
                .map(EventRule::ruleName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
    }

    private List<String> resolveAbnormalMetrics(Alarm alarm) {
        if (alarm.service() == null || alarm.service().isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> metrics = new LinkedHashSet<>();
        for (EventRule rule : eventRuleService.listRules()) {
            if (!alarm.service().equals(rule.service())) {
                continue;
            }
            String metric = rule.metric();
            if (metric != null && !metric.isBlank()) {
                metrics.add(metric);
            }
        }
        return List.copyOf(metrics);
    }

    private record AlarmTiming(long startMillis, Long endMillis, long durationSeconds) {
    }

    private AlarmTiming resolveAlarmTiming(Alarm alarm) {
        long triggerMillis = alarm.triggeredAt().toEpochMilli();
        Long resolvedMillis = alarm.resolvedAt() == null ? triggerMillis : alarm.resolvedAt().toEpochMilli();
        return new AlarmTiming(triggerMillis, resolvedMillis, 0L);
    }

    private static int portalLevel(String level) {
        if ("critical".equalsIgnoreCase(level) || "error".equalsIgnoreCase(level)) {
            return 3;
        }
        return 2;
    }

    private static boolean matchesLevel(Alarm event, List<Integer> levels) {
        if (levels.isEmpty()) {
            return true;
        }
        return levels.contains(portalLevel(event.level()));
    }

    private static boolean matchesDescription(Alarm event, String description) {
        if (description == null) {
            return true;
        }
        String message = event.message();
        return message != null && message.contains(description);
    }

    private static boolean matchesIdLike(Alarm event, String idLike) {
        if (idLike == null) {
            return true;
        }
        return event.id() != null && event.id().contains(idLike);
    }

    private static boolean matchesService(Alarm event, List<String> serviceFilters) {
        if (serviceFilters.isEmpty()) {
            return true;
        }
        return serviceFilters.stream().anyMatch(filter -> PortalServiceIdResolver.matches(filter, event.service()));
    }

    private static boolean matchesRuleName(List<String> ruleNames, List<String> ruleNameFilters) {
        if (ruleNameFilters.isEmpty()) {
            return true;
        }
        return ruleNames.stream().anyMatch(ruleNameFilters::contains);
    }

    private static List<String> resolveTriggerValues(Map<String, Object> body, String key) {
        List<String> values = parseStringList(body.get(key));
        if (!values.isEmpty()) {
            return values;
        }
        if (body.get("trigger") instanceof Map<?, ?> trigger) {
            return parseStringList(trigger.get(key));
        }
        return List.of();
    }

    private static List<String> parseStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(AlarmService::stringValue)
                    .filter(item -> item != null)
                    .toList();
        }
        String single = stringValue(value);
        return single == null ? List.of() : List.of(single);
    }

    private static List<Integer> parseStatuses(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(AlarmService::toInt)
                    .filter(v -> v != null)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        Integer single = toInt(value);
        return single == null ? List.of() : List.of(single);
    }

    private static Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static Comparator<Map<String, Object>> alarmRowComparator(Map<String, Object> body) {
        String sortField = AlarmPolicySupport.stringValue(body.get("sortField"), "timestamp");
        boolean asc = "asc".equalsIgnoreCase(AlarmPolicySupport.stringValue(body.get("sortOrder"), "desc"));
        Comparator<Map<String, Object>> comparator = switch (sortField) {
            case "timestamp", "startTriggerTime", "endTriggerTime", "duration", "eventCnt", "level" ->
                    Comparator.comparing(
                            row -> rowLongValue(row.get(sortField)),
                            Comparator.nullsLast(Long::compareTo));
            case "id", "description", "type", "serviceName", "domainName" ->
                    Comparator.comparing(
                            row -> AlarmPolicySupport.stringValue(row.get(sortField), ""),
                            String.CASE_INSENSITIVE_ORDER);
            default ->
                    Comparator.comparing(
                            row -> rowLongValue(row.get("timestamp")),
                            Comparator.nullsLast(Long::compareTo));
        };
        return asc ? comparator : comparator.reversed();
    }

    private static List<Map<String, Object>> paginateRows(List<Map<String, Object>> rows, Map<String, Object> body) {
        int size = AlarmPolicySupport.intValue(body.get("size"), AlarmPolicySupport.intValue(body.get("pageSize"), 50));
        int offset = AlarmPolicySupport.intValue(body.get("offset"), 0);
        int pageNum = AlarmPolicySupport.intValue(body.get("pageNum"), 0);
        if (pageNum > 0) {
            offset = (pageNum - 1) * size;
        }
        if (size < 1) {
            size = 50;
        }
        if (offset < 0) {
            offset = 0;
        }
        if (offset >= rows.size()) {
            return List.of();
        }
        return rows.subList(offset, Math.min(offset + size, rows.size()));
    }

    private static Long rowLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
