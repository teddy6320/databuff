package com.databuff.apm.common.metric;

import java.util.Map;

/** One row in {@code config_metric_core} (one measurement, all fields in {@code fieldsJson}). */
public record MetricCoreSeedRow(
        long id,
        String type1,
        String type2,
        String type3,
        String app,
        String databaseName,
        String measurement,
        String dorisTable,
        String description,
        Map<String, String> tagKey,
        Map<String, String> tagValue,
        Map<String, Map<String, Object>> fields,
        boolean enabled,
        boolean builtin) {
}
