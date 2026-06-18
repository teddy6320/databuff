package com.databuff.apm.web.metric;

import com.databuff.apm.common.metric.MetricCoreSeedRow;
import com.databuff.apm.common.metric.MetricQueryCatalog;
import com.databuff.apm.common.metric.MetricQueryDefinition;
import com.databuff.apm.common.storage.ApmConfigRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class MetricCoreCatalogService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private volatile NavigableMap<String, MetricQueryDefinition> openMetrics = new TreeMap<>();

    public void reloadFromRows(Iterable<MetricCoreSeedRow> rows) {
        openMetrics = new TreeMap<>(MetricQueryCatalog.expand(rows));
    }

    public void reloadFromRepository(ApmConfigRepository repository) throws Exception {
        List<MetricCoreSeedRow> seeds = new ArrayList<>();
        for (ApmConfigRepository.MetricCoreConfigRow row : repository.loadMetricCoreRows()) {
            seeds.add(fromRow(row));
        }
        reloadFromRows(seeds);
    }

    public MetricQueryDefinition findOpenByIdentifier(String identifier) {
        return identifier == null ? null : openMetrics.get(identifier);
    }

    public Map<String, MetricQueryDefinition> findOpen() {
        return openMetrics;
    }

    public Map<String, MetricQueryDefinition> queryIn(Collection<String> identifiers) {
        Map<String, MetricQueryDefinition> result = new LinkedHashMap<>();
        if (identifiers == null) {
            return result;
        }
        for (String identifier : identifiers) {
            MetricQueryDefinition definition = openMetrics.get(identifier);
            if (definition != null) {
                result.put(identifier, definition);
            }
        }
        return result;
    }

    public Set<String> findMetricByType(String type1, String type2, String type3) {
        Set<String> result = new TreeSet<>();
        for (MetricQueryDefinition definition : openMetrics.values()) {
            if (!matchesType(definition, type1, type2, type3)) {
                continue;
            }
            result.add(definition.getIdentifier());
        }
        return result;
    }

    /**
     * Returns identifier → definition for portal metric pickers
     * ({@code POST /metrics/searchAllMetrics}).
     */
    public Map<String, MetricQueryDefinition> searchAllMetrics(
            String type1, String type2, String type3, String host, String app) {
        Map<String, MetricQueryDefinition> result = new LinkedHashMap<>();
        String normalizedType1 = blankToNull(type1);
        String normalizedType2 = blankToNull(type2);
        String normalizedType3 = blankToNull(type3);
        String normalizedApp = blankToNull(app);
        for (Map.Entry<String, MetricQueryDefinition> entry : openMetrics.entrySet()) {
            MetricQueryDefinition definition = entry.getValue();
            if (!matchesType(definition, normalizedType1, normalizedType2, normalizedType3)) {
                continue;
            }
            if (normalizedApp != null && !normalizedApp.equals(definition.getApp())) {
                continue;
            }
            if (blankToNull(host) != null && !matchesHost(definition, host)) {
                continue;
            }
            result.put(entry.getKey(), definition);
        }
        return result;
    }

    public List<MetricTypeDto> findAllTypes(Boolean builtin) {
        Map<MetricTypeDto, TreeSet<String>> grouped = new LinkedHashMap<>();
        for (MetricQueryDefinition definition : openMetrics.values()) {
            if (builtin != null && !builtin.equals(definition.getBuiltin())) {
                continue;
            }
            MetricTypeDto key = new MetricTypeDto();
            key.setType1(definition.getType1());
            key.setType2(definition.getType2());
            key.setType3(definition.getType3());
            key.setApp(definition.getApp());
            key.setBuiltin(definition.getBuiltin());
            grouped.computeIfAbsent(key, ignored -> new TreeSet<>()).add(definition.getIdentifier());
        }
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    MetricTypeDto dto = entry.getKey();
                    dto.setMetricList(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Aggregates group-by tag keys across all open metrics for portal tag-label maps.
     * Response shape for {@code GET /metrics/query/tagKey/all}.
     */
    public Map<String, Map<String, Object>> allTagKeys() {
        Map<String, TagKeyAggregate> aggregates = new LinkedHashMap<>();
        for (MetricQueryDefinition definition : openMetrics.values()) {
            mergeTagKeys(aggregates, definition.getTagKey(), definition.getTagValue());
        }
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map.Entry<String, TagKeyAggregate> entry : aggregates.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toResponse(entry.getKey()));
        }
        return result;
    }

    private static void mergeTagKeys(
            Map<String, TagKeyAggregate> aggregates,
            Map<String, String> tagKey,
            Map<String, String> tagValue) {
        if (tagKey == null || tagKey.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : tagKey.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            aggregates.computeIfAbsent(key, ignored -> new TagKeyAggregate())
                    .mergeLabel(key, entry.getValue());
        }
        if (tagValue != null) {
            for (Map.Entry<String, String> entry : tagValue.entrySet()) {
                String key = entry.getKey();
                if (key == null || key.isBlank()) {
                    continue;
                }
                aggregates.computeIfAbsent(key, ignored -> new TagKeyAggregate())
                        .mergeEnumValue(entry.getKey(), entry.getValue());
            }
        }
    }

    public List<MetricTypeDto> searchMetricTypes(String typeKey, String metricKey) {
        String lowerType = typeKey == null ? "" : typeKey.trim().toLowerCase();
        String lowerMetric = metricKey == null ? "" : metricKey.trim().toLowerCase();
        Map<MetricTypeDto, TreeSet<String>> grouped = new LinkedHashMap<>();
        for (MetricQueryDefinition definition : openMetrics.values()) {
            if (!typeMatches(definition, lowerType) || !metricMatches(definition, lowerMetric)) {
                continue;
            }
            MetricTypeDto key = new MetricTypeDto();
            key.setType1(definition.getType1());
            key.setType2(definition.getType2());
            key.setType3(definition.getType3());
            key.setApp(definition.getApp());
            key.setBuiltin(definition.getBuiltin());
            grouped.computeIfAbsent(key, ignored -> new TreeSet<>()).add(definition.getIdentifier());
        }
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    MetricTypeDto dto = entry.getKey();
                    dto.setMetricList(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private static boolean matchesType(MetricQueryDefinition definition, String type1, String type2, String type3) {
        return (type1 == null || type1.equals(definition.getType1()))
                && (type2 == null || type2.equals(definition.getType2()))
                && (type3 == null || type3.equals(definition.getType3()));
    }

    private static boolean matchesHost(MetricQueryDefinition definition, String host) {
        // Host-scoped metric lists are filtered primarily by {@code app}; hostname is kept for API compatibility.
        return true;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static boolean typeMatches(MetricQueryDefinition definition, String lowerType) {
        if (lowerType.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(definition.getType1(), lowerType)
                || containsIgnoreCase(definition.getType2(), lowerType)
                || containsIgnoreCase(definition.getType3(), lowerType);
    }

    private static boolean metricMatches(MetricQueryDefinition definition, String lowerMetric) {
        if (lowerMetric.isEmpty()) {
            return true;
        }
        String identifier = definition.getIdentifier() == null ? "" : definition.getIdentifier().toLowerCase();
        String metricCn = definition.getMetricCn() == null ? "" : definition.getMetricCn().toLowerCase();
        return identifier.contains(lowerMetric) || metricCn.contains(lowerMetric);
    }

    private static boolean containsIgnoreCase(String value, String lowerNeedle) {
        return value != null && value.toLowerCase().contains(lowerNeedle);
    }

    private static MetricCoreSeedRow fromRow(ApmConfigRepository.MetricCoreConfigRow row) throws Exception {
        return new MetricCoreSeedRow(
                row.id(),
                row.type1(),
                row.type2(),
                row.type3(),
                row.app(),
                row.databaseName(),
                row.measurement(),
                row.dorisTable(),
                row.description(),
                readMap(row.tagKeyJson()),
                readStringMap(row.tagValueJson()),
                readFields(row.fieldsJson()),
                row.enabled(),
                row.builtin());
    }

    private static Map<String, String> readStringMap(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        return MAPPER.readValue(json, new TypeReference<>() {
        });
    }

    private static Map<String, String> readMap(String json) throws Exception {
        return readStringMap(json);
    }

    private static Map<String, Map<String, Object>> readFields(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        return MAPPER.readValue(json, new TypeReference<>() {
        });
    }

    private static final class TagKeyAggregate {
        private String name = "";
        private final Map<String, String> tagValue = new LinkedHashMap<>();

        void mergeLabel(String key, String label) {
            if (name.isBlank()) {
                name = label == null || label.isBlank() ? key : label;
                return;
            }
            if (label != null && !label.isBlank() && label.length() > name.length()) {
                name = label;
            }
        }

        void mergeEnumValue(String value, String label) {
            if (value == null || label == null) {
                return;
            }
            tagValue.putIfAbsent(value, label);
        }

        Map<String, Object> toResponse(String key) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("enabled", !key.startsWith("envTag"));
            row.put("name", name.isBlank() ? key : name);
            if (!tagValue.isEmpty()) {
                row.put("tagValue", new LinkedHashMap<>(tagValue));
            }
            return row;
        }
    }
}
