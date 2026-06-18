package com.databuff.apm.common.meta;

import com.databuff.apm.common.model.DcSpan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Parse OTLP attribute maps stored on spans / metric lines. */
public final class OtelAttributeMaps {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() {
    };

    private OtelAttributeMaps() {
    }

    public static Map<String, String> parse(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, String> parsed = JSON.readValue(json, STRING_MAP);
            return parsed == null ? Map.of() : parsed;
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    public static Map<String, String> parse(DcSpan span) {
        if (span == null) {
            return Map.of();
        }
        if (span.metaAttributes != null && Objects.equals(span.metaAttributesSource, span.meta)) {
            return span.metaAttributes;
        }
        Map<String, String> parsed = parse(span.meta);
        span.metaAttributes = parsed;
        span.metaAttributesSource = span.meta;
        span.metaAttributesDirty = false;
        return parsed;
    }

    public static Map<String, String> mutableCopy(DcSpan span) {
        return new LinkedHashMap<>(parse(span));
    }

    public static void cache(DcSpan span, Map<String, String> attributes) {
        if (span == null) {
            return;
        }
        span.metaAttributes = attributes == null ? Map.of() : attributes;
        span.metaAttributesSource = span.meta;
        span.metaAttributesDirty = false;
    }

    public static void replace(DcSpan span, Map<String, String> attributes) {
        if (span == null) {
            return;
        }
        span.metaAttributes = attributes == null || attributes.isEmpty() ? Map.of() : attributes;
        span.metaAttributesSource = span.meta;
        span.metaAttributesDirty = true;
        span.analysisCache = null;
    }

    public static void materialize(DcSpan span) {
        if (span == null || !span.metaAttributesDirty) {
            return;
        }
        span.meta = encode(span.metaAttributes);
        span.metaAttributesSource = span.meta;
        span.metaAttributesDirty = false;
    }

    public static String encode(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }
        try {
            return JSON.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static String firstNonBlank(Map<String, String> attributes, String... keys) {
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
