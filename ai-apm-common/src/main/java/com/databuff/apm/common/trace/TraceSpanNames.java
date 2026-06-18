package com.databuff.apm.common.trace;

import com.databuff.apm.common.meta.OtelAttributeMaps;

import java.util.Locale;
import java.util.Map;

/**
 * DataBuff trace span names; legacy portal compatibility ({@code Constant.Trace}).
 */
public final class TraceSpanNames {

    public static final String ES_REST_QUERY = "elasticsearch.rest.query";
    public static final String ES_QUERY = "elasticsearch.query";

    private TraceSpanNames() {
    }

    public static boolean isElasticsearchSpanName(String name) {
        return ES_REST_QUERY.equals(name) || ES_QUERY.equals(name);
    }

    public static boolean isElasticsearchDbSystem(String dbSystem) {
        return dbSystem != null && dbSystem.toLowerCase(Locale.ROOT).contains("elastic");
    }

    public static boolean isElasticsearchMeta(Map<String, String> meta) {
        String db = OtelAttributeMaps.firstNonBlank(meta, "db.system", "db.type");
        return isElasticsearchDbSystem(db);
    }

    /**
     * Map OTel elasticsearch spans to DataBuff span names for downstream processors.
     */
    public static String normalizeOtelName(String otelName, Map<String, String> attributes) {
        if (!isElasticsearchMeta(attributes)) {
            return otelName;
        }
        if (hasHttpSemanticAttributes(attributes)) {
            return ES_REST_QUERY;
        }
        return ES_QUERY;
    }

    private static boolean hasHttpSemanticAttributes(Map<String, String> attributes) {
        return OtelAttributeMaps.firstNonBlank(attributes, "http.method") != null
                || OtelAttributeMaps.firstNonBlank(attributes, "http.url", "url.full") != null;
    }
}
