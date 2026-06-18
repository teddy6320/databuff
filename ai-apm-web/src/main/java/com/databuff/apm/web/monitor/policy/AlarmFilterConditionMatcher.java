package com.databuff.apm.web.monitor.policy;

import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.pipeline.EventRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AlarmFilterConditionMatcher {

    private AlarmFilterConditionMatcher() {
    }

    public static boolean matches(List<?> conditions, Alarm alarm, EventRecord event) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        return evaluateGroup(conditions, buildFieldValues(alarm, event));
    }

    private static Map<String, String> buildFieldValues(Alarm alarm, EventRecord event) {
        Map<String, String> values = new LinkedHashMap<>();
        if (alarm != null) {
            values.put("description", nullToEmpty(alarm.message()));
            values.put("level", String.valueOf(portalLevel(alarm.level())));
            values.put("service", nullToEmpty(alarm.service()));
            values.put("serviceId", nullToEmpty(alarm.service()));
        }
        if (event != null) {
            values.put("ruleName", nullToEmpty(event.ruleName()));
            values.put("classification", "singleMetric");
        }
        return values;
    }

    private static boolean evaluateGroup(List<?> conditions, Map<String, String> fieldValues) {
        Boolean result = null;
        for (Object item : conditions) {
            if (!(item instanceof Map<?, ?> condition)) {
                continue;
            }
            boolean current = evaluateCondition(condition, fieldValues);
            String connector = stringValue(condition.get("connector"), "AND");
            if (result == null) {
                result = current;
            } else if ("OR".equalsIgnoreCase(connector)) {
                result = result || current;
            } else {
                result = result && current;
            }
        }
        return result != null && result;
    }

    private static boolean evaluateCondition(Map<?, ?> condition, Map<String, String> fieldValues) {
        Object left = condition.get("left");
        if (left instanceof List<?> nested) {
            return evaluateGroup(nested, fieldValues);
        }
        String field = String.valueOf(left);
        String operator = stringValue(condition.get("operator"), "=");
        String expected = stringValue(condition.get("right"), "");
        String actual = fieldValues.getOrDefault(field, "");
        return matchOperator(actual, operator, expected);
    }

    static boolean matchOperator(String actual, String operator, String expected) {
        String left = nullToEmpty(actual);
        String right = nullToEmpty(expected);
        return switch (normalizeOperator(operator)) {
            case "!=" -> !left.equals(right);
            case "LIKE" -> left.contains(right);
            case "NOT LIKE" -> !left.contains(right);
            default -> left.equals(right);
        };
    }

    private static String normalizeOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            return "=";
        }
        return switch (operator.trim().toLowerCase(Locale.ROOT)) {
            case "!=", "neq" -> "!=";
            case "like" -> "LIKE";
            case "notlike", "not_like", "not like" -> "NOT LIKE";
            default -> "=";
        };
    }

    private static int portalLevel(String level) {
        if ("critical".equalsIgnoreCase(level) || "error".equalsIgnoreCase(level)) {
            return 3;
        }
        return 2;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }
}
