package com.databuff.apm.web.tools.local;

import com.databuff.apm.common.time.ApmTimeZones;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Formats metric tool rows for LLM-readable wall-clock timestamps. */
final class MetricToolResultFormat {

    static final String EPOCH_SEC_KEY = "epoch_sec";
    static final String TIME_KEY = "time";

    private MetricToolResultFormat() {
    }

    static List<Map<String, Object>> formatEpochSecRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return rows == null ? List.of() : rows;
        }
        List<Map<String, Object>> formatted = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            formatted.add(formatEpochSecRow(row));
        }
        return formatted;
    }

    static Map<String, Object> formatEpochSecRow(Map<String, Object> row) {
        if (row == null || !row.containsKey(EPOCH_SEC_KEY)) {
            return row;
        }
        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put(TIME_KEY, formatEpochSec(row.get(EPOCH_SEC_KEY)));
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (!EPOCH_SEC_KEY.equals(entry.getKey())) {
                formatted.put(entry.getKey(), entry.getValue());
            }
        }
        return formatted;
    }

    private static String formatEpochSec(Object value) {
        if (value instanceof Number number) {
            return ApmTimeZones.formatWallClock(number.longValue() * 1000L);
        }
        try {
            return ApmTimeZones.formatWallClock(Long.parseLong(String.valueOf(value)) * 1000L);
        } catch (NumberFormatException ignored) {
            return String.valueOf(value);
        }
    }
}
