package com.databuff.apm.ingest.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Maps OpenTelemetry JVM runtime metrics (legacy {@code process.runtime.jvm.*} and
 * semconv {@code jvm.*}) onto DataBuff flat identifiers such as {@code jvm.memory.heap.used}.
 */
public final class JvmOtelMetricNormalizer {

    public record NormalizedMetric(String identifier, Number value) {
    }

    private JvmOtelMetricNormalizer() {
    }

    public static Optional<String> normalizeIdentifier(String otelName, Map<String, String> attributes) {
        return normalize(otelName, attributes, 0).stream()
                .map(NormalizedMetric::identifier)
                .findFirst();
    }

    public static List<NormalizedMetric> normalize(String otelName, Map<String, String> attributes, Number value) {
        if (otelName == null || otelName.isBlank()) {
            return List.of();
        }
        if (isDatabuffJvmIdentifier(otelName)) {
            return List.of(new NormalizedMetric(otelName, value));
        }
        return switch (otelName) {
            case "process.runtime.jvm.threads.count", "jvm.threads.count", "jvm.thread.count" ->
                    List.of(new NormalizedMetric("jvm.thread_count", value));
            case "process.runtime.jvm.cpu.utilization", "jvm.cpu.recent_utilization" ->
                    List.of(new NormalizedMetric("jvm.cpu_load_process", value));
            case "process.runtime.jvm.system.cpu.utilization", "jvm.system.cpu.utilization" ->
                    List.of(new NormalizedMetric("jvm.cpu_load_system", value));
            case "process.runtime.jvm.classes.loaded", "jvm.classes.loaded", "jvm.classes.count",
                 "jvm.class.loaded", "jvm.class.count" ->
                    List.of(new NormalizedMetric("jvm.loaded_classes.count", value));
            case "process.runtime.jvm.memory.usage", "jvm.memory.used" ->
                    mapMemoryUsed(attributes, value);
            case "process.runtime.jvm.memory.committed", "jvm.memory.committed" ->
                    mapMemoryCommitted(attributes, value);
            case "process.runtime.jvm.memory.limit", "jvm.memory.limit" ->
                    mapMemoryLimit(attributes, value);
            case "process.runtime.jvm.memory.init", "jvm.memory.init" ->
                    mapMemoryInit(attributes, value);
            case "process.runtime.jvm.buffer.usage", "jvm.buffer.memory.used", "jvm.buffer.memory.usage" ->
                    mapBufferMetric(attributes, value, "used");
            case "process.runtime.jvm.buffer.limit", "jvm.buffer.memory.limit" ->
                    mapBufferMetric(attributes, value, "capacity");
            case "process.runtime.jvm.buffer.count", "jvm.buffer.count" ->
                    mapBufferMetric(attributes, value, "count");
            default -> List.of();
        };
    }

    public static List<NormalizedMetric> normalizeHistogram(
            String otelName,
            Map<String, String> attributes,
            double sum,
            long count) {
        if (!"process.runtime.jvm.gc.duration".equals(otelName) && !"jvm.gc.duration".equals(otelName)) {
            return List.of();
        }
        String action = attribute(attributes, "jvm.gc.action", "action");
        if (action == null) {
            return List.of();
        }
        String lower = action.toLowerCase(Locale.ROOT);
        List<NormalizedMetric> out = new ArrayList<>(2);
        if (lower.contains("minor")) {
            out.add(new NormalizedMetric("jvm.gc.minor_collection_count", count));
            out.add(new NormalizedMetric("jvm.gc.minor_collection_time", sum));
            return out;
        }
        if (lower.contains("major")) {
            out.add(new NormalizedMetric("jvm.gc.major_collection_count", count));
            out.add(new NormalizedMetric("jvm.gc.major_collection_time", sum));
        }
        return out;
    }

    private static List<NormalizedMetric> mapMemoryUsed(Map<String, String> attributes, Number value) {
        Optional<String> poolTarget = mapPoolUsedTarget(attributes);
        if (poolTarget.isPresent()) {
            return List.of(new NormalizedMetric(poolTarget.get(), value));
        }
        String memoryType = memoryType(attributes);
        if ("heap".equals(memoryType)) {
            return List.of(new NormalizedMetric("jvm.memory.heap.used", value));
        }
        if ("non_heap".equals(memoryType)) {
            return List.of(new NormalizedMetric("jvm.memory.noheap.used", value));
        }
        return List.of();
    }

    private static List<NormalizedMetric> mapMemoryCommitted(Map<String, String> attributes, Number value) {
        return switch (memoryType(attributes)) {
            case "heap" -> List.of(new NormalizedMetric("jvm.memory.heap.committed", value));
            case "non_heap" -> List.of(new NormalizedMetric("jvm.memory.noheap.committed", value));
            default -> List.of();
        };
    }

    private static List<NormalizedMetric> mapMemoryLimit(Map<String, String> attributes, Number value) {
        return switch (memoryType(attributes)) {
            case "heap" -> List.of(new NormalizedMetric("jvm.memory.heap.max", value));
            case "non_heap" -> List.of(new NormalizedMetric("jvm.memory.noheap.max", value));
            default -> List.of();
        };
    }

    private static List<NormalizedMetric> mapMemoryInit(Map<String, String> attributes, Number value) {
        return switch (memoryType(attributes)) {
            case "heap" -> List.of(new NormalizedMetric("jvm.memory.heap.init", value));
            case "non_heap" -> List.of(new NormalizedMetric("jvm.memory.noheap.init", value));
            default -> List.of();
        };
    }

    private static Optional<String> mapPoolUsedTarget(Map<String, String> attributes) {
        String pool = poolName(attributes);
        if (pool == null) {
            return Optional.empty();
        }
        String lower = pool.toLowerCase(Locale.ROOT);
        if (lower.contains("eden")) {
            return Optional.of("jvm.gc.eden_size");
        }
        if (lower.contains("survivor")) {
            return Optional.of("jvm.gc.survivor_size");
        }
        if (lower.contains("old") || lower.contains("tenured")) {
            return Optional.of("jvm.gc.old_gen_size");
        }
        if (lower.contains("metaspace") || lower.contains("perm gen")) {
            return Optional.of("jvm.gc.metaspace_size");
        }
        return Optional.empty();
    }

    private static List<NormalizedMetric> mapBufferMetric(
            Map<String, String> attributes,
            Number value,
            String field) {
        String pool = attribute(attributes, "jvm.buffer.pool.name", "pool");
        if (pool == null) {
            return List.of();
        }
        String lower = pool.toLowerCase(Locale.ROOT);
        if (lower.contains("direct")) {
            return List.of(new NormalizedMetric("jvm.buffer_pool.direct." + field, value));
        }
        if (lower.contains("mapped")) {
            return List.of(new NormalizedMetric("jvm.buffer_pool.mapped." + field, value));
        }
        return List.of();
    }

    private static String memoryType(Map<String, String> attributes) {
        String raw = attribute(attributes, "jvm.memory.type", "type");
        if (raw == null) {
            return null;
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if ("heap".equals(lower)) {
            return "heap";
        }
        if ("non_heap".equals(lower) || "nonheap".equals(lower)) {
            return "non_heap";
        }
        return null;
    }

    private static String poolName(Map<String, String> attributes) {
        return attribute(attributes, "jvm.memory.pool.name", "pool");
    }

    private static boolean isDatabuffJvmIdentifier(String name) {
        if ("jvm.thread_count".equals(name)
                || "jvm.cpu_load_process".equals(name)
                || "jvm.cpu_load_system".equals(name)) {
            return true;
        }
        if (name.startsWith("jvm.memory.heap.")
                || name.startsWith("jvm.memory.noheap.")
                || name.startsWith("jvm.buffer_pool.")
                || name.startsWith("jvm.loaded_classes.")) {
            return true;
        }
        if (!name.startsWith("jvm.gc.")) {
            return false;
        }
        return switch (name.substring("jvm.gc.".length())) {
            case "eden_size", "major_collection_count", "major_collection_time", "metaspace_size",
                 "minor_collection_count", "minor_collection_time", "old_gen_size", "survivor_size" -> true;
            default -> false;
        };
    }

    private static String attribute(Map<String, String> attributes, String... keys) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }
        for (String key : keys) {
            String value = attributes.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
