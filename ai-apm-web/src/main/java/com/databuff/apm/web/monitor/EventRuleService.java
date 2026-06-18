package com.databuff.apm.web.monitor;

import com.databuff.apm.web.monitor.eval.EventRulePayloadParser;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class EventRuleService {

    private final EventRuleStore store;

    public EventRuleService(EventRuleStore store) {
        this.store = store;
    }

    public List<EventRule> listRules() {
        return store.list();
    }

    public List<String> collectRuleGroupByFields() {
        Set<String> fields = new LinkedHashSet<>();
        for (EventRule rule : listRules()) {
            EventRulePayloadParser.extractGroupByFields(rule.queryJson()).forEach(fields::add);
        }
        return List.copyOf(fields);
    }

    public Optional<EventRule> findRule(long id) {
        return store.findById(id);
    }

    public EventRule createRule(EventRuleStore.CreateRequest request) {
        validateCreate(request);
        return store.create(request);
    }

    public EventRule saveRule(EventRule existing, Map<String, Object> body) {
        EventRulePayloadParser.DerivedFields derived = EventRulePayloadParser.derive(body);
        String ruleName = stringValue(body.get("ruleName"), stringValue(body.get("name"), existing.ruleName()));
        boolean enabled = body.containsKey("enabled")
                ? boolValue(body.get("enabled"), existing.enabled())
                : existing.enabled();
        EventRule updated = new EventRule(
                existing.id(),
                ruleName,
                derived.classify(),
                derived.detectionWay(),
                derived.service(),
                derived.metric(),
                derived.threshold(),
                derived.comparator(),
                enabled,
                derived.queryJson() != null ? derived.queryJson() : existing.queryJson(),
                java.time.Instant.now());
        return store.save(updated);
    }

    public EventRule createRuleFromBody(Map<String, Object> body) {
        EventRulePayloadParser.DerivedFields derived = EventRulePayloadParser.derive(body);
        return createRule(new EventRuleStore.CreateRequest(
                stringValue(body.get("ruleName"), stringValue(body.get("name"), "rule")),
                derived.classify(),
                derived.service(),
                derived.metric(),
                derived.threshold(),
                derived.comparator(),
                boolValue(body.get("enabled"), true),
                derived.detectionWay(),
                derived.queryJson()));
    }

    public Optional<EventRule> updateEnabled(long id, boolean enabled) {
        return store.findById(id).map(rule -> store.save(rule.withEnabled(enabled)));
    }

    public boolean deleteRule(long id) {
        return store.delete(id);
    }

    private static String stringValue(Object primary, String fallback) {
        String value = primary == null ? null : String.valueOf(primary).trim();
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return fallback;
    }

    private static boolean boolValue(Object value, boolean defaultValue) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(String.valueOf(value).trim());
    }

    private static void validateCreate(EventRuleStore.CreateRequest request) {
        if (request.ruleName() == null || request.ruleName().isBlank()) {
            throw new IllegalArgumentException("ruleName is required");
        }
        String metric = request.metric() == null || request.metric().isBlank()
                ? EventRule.METRIC_ERROR_RATE
                : request.metric();
        if (EventRule.METRIC_ERROR_RATE.equals(metric)
                && (request.threshold() < 0 || request.threshold() > 1)
                && (request.queryJson() == null || request.queryJson().isBlank())) {
            throw new IllegalArgumentException("threshold must be between 0 and 1 for error_rate");
        }
    }
}
