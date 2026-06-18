package com.databuff.apm.common.storage;

import com.databuff.apm.common.metric.MetricSchemaRegistry;

/** Parses portal-style metric identifiers into Doris measurement + field. */
public final class MetricIdentifierParser {

    private MetricIdentifierParser() {
    }

    public record ParsedMetric(String measurement, String field) {
    }

    public static ParsedMetric parse(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("metric identifier is blank");
        }
        String normalized = normalizeLegacyJvmIdentifier(identifier);
        return MetricSchemaRegistry.measurementsSortedLongestFirst().stream()
                .filter(measurement -> normalized.equals(measurement) || normalized.startsWith(measurement + "."))
                .findFirst()
                .map(measurement -> {
                    if (normalized.equals(measurement)) {
                        return new ParsedMetric(measurement, defaultField(measurement));
                    }
                    return new ParsedMetric(measurement, normalized.substring(measurement.length() + 1));
                })
                .orElseThrow(() -> new IllegalArgumentException("unsupported metric: " + identifier));
    }

    private static String normalizeLegacyJvmIdentifier(String identifier) {
        return switch (identifier) {
            case "jvm.cpu_load.process" -> "jvm.cpu_load_process";
            case "jvm.cpu_load.system" -> "jvm.cpu_load_system";
            default -> identifier;
        };
    }

    private static String defaultField(String measurement) {
        return MetricSchemaRegistry.schema(measurement)
                .flatMap(schema -> schema.fields().stream().findFirst())
                .orElse("cnt");
    }

    public static String toColumnName(String tag) {
        return MetricSchemaRegistry.toColumnName(tag);
    }

    public static String toFieldColumnName(String field) {
        return MetricSchemaRegistry.toFieldColumnName(field);
    }

    /** Doris column for a parsed JVM (or other) metric — qualifies sub-measurements on {@code metric_jvm}. */
    public static String toDorisFieldColumn(String measurement, String field) {
        if (measurement == null || field == null || field.isBlank()) {
            return field;
        }
        String column;
        if ("jvm".equals(measurement)) {
            column = field;
        } else if (measurement.startsWith("jvm.")) {
            column = measurement.substring("jvm.".length()) + "." + field;
        } else {
            return field;
        }
        return column.contains(".") ? MetricSchemaRegistry.streamLoadJsonKey(column) : column;
    }

    public static String toDorisFieldColumn(ParsedMetric parsed) {
        return toDorisFieldColumn(parsed.measurement(), parsed.field());
    }

    public static String dorisTableName(String measurement) {
        return MetricSchemaRegistry.tableName(measurement);
    }
}
