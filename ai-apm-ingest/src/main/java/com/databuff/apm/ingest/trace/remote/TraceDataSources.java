package com.databuff.apm.ingest.trace.remote;

import java.util.Locale;
import java.util.Set;

/**
 * Trace data source constants; legacy portal compatibility ({@code Constant.Trace}).
 */
public final class TraceDataSources {

    public static final String META_DATA_SOURCE = "data.source";

    public static final String DATABUFF = "Databuff";
    public static final String DF_JAVA_AGENT = "DF-javaagent";
    public static final String DF_DOTNET = "DF-donet";
    public static final String DF_EBPF = "DF-ebpf";
    public static final String OTEL = "Otel";
    public static final String SKY_WALKING = "SkyWalking";

    public static final Set<String> DATABUFF_SOURCES = Set.of(
            DF_JAVA_AGENT, DF_DOTNET, DATABUFF, DF_EBPF);

    private TraceDataSources() {
    }

    public static String resolve(java.util.Map<String, String> meta) {
        if (meta == null) {
            return OTEL;
        }
        String source = firstNonBlank(
                meta.get(META_DATA_SOURCE),
                meta.get("datasource"),
                meta.get("telemetry.datasource"));
        if (source == null || source.isBlank()) {
            return OTEL;
        }
        return source.trim();
    }

    public static boolean isDatabuffSource(String dataSource) {
        if (dataSource == null || dataSource.isBlank()) {
            return false;
        }
        String normalized = dataSource.trim();
        if (DATABUFF_SOURCES.contains(normalized)) {
            return true;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        return lower.contains("databuff") || lower.startsWith("df-");
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
