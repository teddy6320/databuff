package com.databuff.apm.common.flow;

import com.databuff.apm.common.serde.DcSpanUtil;
import com.databuff.apm.common.model.DcSpan;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Service-flow span/resource rules; legacy portal virtual span handling. */
public final class ServiceFlowSpanRules {

    private static final Pattern SQL_RESOURCE = Pattern.compile(
            "^(?i)(SELECT|INSERT|UPDATE|DELETE|MERGE|CALL|CREATE|DROP|ALTER|TRUNCATE|REPLACE|WITH)\\b");

    private static final Set<String> REDIS_COMMANDS = Set.of(
            "GET", "SET", "HGET", "HSET", "DEL", "EXPIRE", "INCR", "DECR", "MGET", "MSET");

    private ServiceFlowSpanRules() {
    }

    public static boolean isVirtualSpan(DcSpan span) {
        if (span == null) {
            return true;
        }
        return DcSpanUtil.isDbSpan(span)
                || DcSpanUtil.isRedisSpan(span)
                || DcSpanUtil.isMqSpan(span)
                || DcSpanUtil.isEsSpan(span)
                || DcSpanUtil.isConfigSpan(span);
    }

    /** Virtual/component service names use {@code [type]name} prefix (legacy portal {@code TraceUtil.getCustomName}). */
    public static boolean isVirtualServiceSpan(DcSpan span) {
        if (span == null || span.service == null || span.service.isBlank()) {
            return false;
        }
        return span.service.charAt(0) == '[';
    }

    public static String displayResource(DcSpan span) {
        if (isVirtualSpan(span) || isVirtualServiceSpan(span)) {
            return "";
        }
        if (span.resource != null && !span.resource.isBlank()) {
            return span.resource;
        }
        return span.name == null ? "" : span.name;
    }

    /** Filters component resources when aggregating portal service-flow trees. */
    public static boolean isComponentResource(String resource) {
        if (resource == null || resource.isBlank()) {
            return true;
        }
        String trimmed = resource.trim();
        if (SQL_RESOURCE.matcher(trimmed).find()) {
            return true;
        }
        if (isHttpLikeResource(trimmed)) {
            return false;
        }
        String firstToken = trimmed.contains(" ") ? trimmed.substring(0, trimmed.indexOf(' ')) : trimmed;
        return REDIS_COMMANDS.contains(firstToken.toUpperCase(Locale.ROOT));
    }

    private static boolean isHttpLikeResource(String resource) {
        int space = resource.indexOf(' ');
        if (space <= 0) {
            return false;
        }
        String method = resource.substring(0, space).toUpperCase(Locale.ROOT);
        if (!Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE").contains(method)) {
            return false;
        }
        return resource.substring(space + 1).startsWith("/");
    }
}
