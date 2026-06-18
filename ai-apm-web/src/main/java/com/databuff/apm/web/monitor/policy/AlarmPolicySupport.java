package com.databuff.apm.web.monitor.policy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class AlarmPolicySupport {

    private AlarmPolicySupport() {
    }

    public static List<Map<String, Object>> filterSortPage(
            List<Map<String, Object>> rows,
            Map<String, Object> body,
            Predicate<Map<String, Object>> matcher,
            String defaultSortField) {
        List<Map<String, Object>> filtered = rows.stream().filter(matcher).toList();
        String sortField = stringValue(body.get("sortField"), defaultSortField);
        boolean asc = "ASC".equalsIgnoreCase(stringValue(body.get("sortOrder"), "DESC"));
        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                row -> stringValue(row.get(sortField), ""),
                String.CASE_INSENSITIVE_ORDER);
        if (!asc) {
            comparator = comparator.reversed();
        }
        filtered = new ArrayList<>(filtered);
        filtered.sort(comparator);
        int pageNum = intValue(body.get("pageNum"), 1);
        int pageSize = intValue(body.get("pageSize"), 20);
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        int from = Math.min((pageNum - 1) * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        return filtered.subList(from, to);
    }

    public static long count(List<Map<String, Object>> rows, Predicate<Map<String, Object>> matcher) {
        return rows.stream().filter(matcher).count();
    }

    public static Map<String, Object> pageEnvelope(List<Map<String, Object>> rows, long total) {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("list", rows);
        page.put("total", total);
        page.put("pageNum", 1);
        page.put("pageSize", rows.size());
        return page;
    }

    public static long nowMillis() {
        return Instant.now().toEpochMilli();
    }

    public static long toMillis(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static int intValue(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long longValue(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    public static boolean boolValue(Object value, boolean defaultValue) {
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

    public static List<Integer> intList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Integer> ids = new ArrayList<>();
        for (Object item : list) {
            ids.add(intValue(item, -1));
        }
        return ids;
    }

    public static boolean keywordMatch(Map<String, Object> row, String keyword, String... fields) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String lower = keyword.toLowerCase(Locale.ROOT);
        for (String field : fields) {
            Object value = row.get(field);
            if (value != null && String.valueOf(value).toLowerCase(Locale.ROOT).contains(lower)) {
                return true;
            }
        }
        return false;
    }

    public static boolean enabledMatch(Map<String, Object> row, Object enabledFilter) {
        if (enabledFilter == null || Objects.equals(enabledFilter, "")) {
            return true;
        }
        return boolValue(row.get("enabled"), false) == boolValue(enabledFilter, false);
    }

    public static Map<String, Object> copyOf(Map<String, Object> source) {
        return new LinkedHashMap<>(source);
    }
}
