package com.databuff.apm.web.flow;

import com.databuff.apm.common.util.PortalServiceIdResolver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Builds portal trace-detail service flow trees from trace span rows. */
public final class TraceServiceFlowBuilder {

    private TraceServiceFlowBuilder() {
    }

    public static Map<String, Object> build(List<Map<String, Object>> spans) {
        if (spans == null || spans.isEmpty()) {
            return Map.of();
        }

        Map<String, Map<String, Object>> spanById = new LinkedHashMap<>();
        Map<String, List<Map<String, Object>>> childrenByParent = new LinkedHashMap<>();
        for (Map<String, Object> span : spans) {
            String spanId = stringValue(span.get("span_id"));
            if (spanId.isBlank()) {
                continue;
            }
            spanById.put(spanId, span);
            String parentId = parentId(span);
            childrenByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(span);
        }

        Map<String, Object> rootSpan = findRootSpan(spanById, childrenByParent);
        if (rootSpan == null) {
            return Map.of();
        }

        long rootDuration = traceDurationNs(spans, rootSpan);
        Set<String> visitedServices = new LinkedHashSet<>();
        visitedServices.add(serviceId(rootSpan));
        Map<String, Object> rootNode = buildNode(rootSpan, childrenByParent, rootDuration, visitedServices, true);
        rootNode.put("callPct", 100.0);
        return rootNode;
    }

    private static Map<String, Object> buildNode(
            Map<String, Object> entrySpan,
            Map<String, List<Map<String, Object>>> childrenByParent,
            long rootDuration,
            Set<String> visitedServices,
            boolean root) {
        String service = stringValue(entrySpan.get("service"));
        String serviceId = serviceId(entrySpan);
        long duration = serviceSubtreeDuration(entrySpan, childrenByParent, service);
        long error = serviceSubtreeError(entrySpan, childrenByParent, service);
        long call = root ? 1L : 1L;
        long srcCall = 1L;

        List<Map<String, Object>> children = new ArrayList<>();
        collectCrossServiceChildren(entrySpan, childrenByParent, rootDuration, visitedServices, children);

        long outCall = 0L;
        for (Map<String, Object> child : children) {
            outCall += longValue(child.get("call"));
        }

        Map<String, Object> node = new LinkedHashMap<>();
        node.put("uid", stringValue(entrySpan.get("span_id")));
        node.put("serviceId", serviceId);
        node.put("service", service);
        node.put("name", service);
        node.put("hostId", stringValue(entrySpan.get("hostName")));
        node.put("call", call);
        node.put("srcCall", srcCall);
        node.put("error", error);
        node.put("duration", duration);
        node.put("avgDuration", duration);
        node.put("avgCall", 1.0);
        node.put("callPct", 0.0);
        node.put("durationCvPct", rootDuration == 0L
                ? 0.0
                : ratio(duration, rootDuration));
        node.put("outCall", outCall);
        node.put("serviceDurationRange", List.of());
        node.put("serviceInstanceMap", Map.of());
        node.put("children", children);
        return node;
    }

    private static void collectCrossServiceChildren(
            Map<String, Object> span,
            Map<String, List<Map<String, Object>>> childrenByParent,
            long rootDuration,
            Set<String> visitedServices,
            List<Map<String, Object>> children) {
        String spanId = stringValue(span.get("span_id"));
        List<Map<String, Object>> childSpans = childrenByParent.getOrDefault(spanId, List.of());
        String currentService = stringValue(span.get("service"));
        for (Map<String, Object> childSpan : childSpans) {
            String childService = stringValue(childSpan.get("service"));
            if (currentService.equals(childService)) {
                collectCrossServiceChildren(childSpan, childrenByParent, rootDuration, visitedServices, children);
                continue;
            }
            String childServiceId = serviceId(childSpan);
            if (visitedServices.contains(childServiceId)) {
                continue;
            }
            visitedServices.add(childServiceId);
            Map<String, Object> childNode =
                    buildNode(childSpan, childrenByParent, rootDuration, visitedServices, false);
            childNode.put("callPct", ratio(childNode.get("call"), 1L));
            childNode.put("durationCvPct", ratio(longValue(childNode.get("duration")), rootDuration));
            childNode.put("avgCall", 1.0);
            children.add(childNode);
        }
    }

    private static long serviceSubtreeDuration(
            Map<String, Object> entrySpan,
            Map<String, List<Map<String, Object>>> childrenByParent,
            String service) {
        long total = longValue(entrySpan.get("duration"));
        String spanId = stringValue(entrySpan.get("span_id"));
        for (Map<String, Object> childSpan : childrenByParent.getOrDefault(spanId, List.of())) {
            if (service.equals(stringValue(childSpan.get("service")))) {
                total += serviceSubtreeDuration(childSpan, childrenByParent, service);
            }
        }
        return total;
    }

    private static long serviceSubtreeError(
            Map<String, Object> entrySpan,
            Map<String, List<Map<String, Object>>> childrenByParent,
            String service) {
        long total = longValue(entrySpan.get("error"));
        String spanId = stringValue(entrySpan.get("span_id"));
        for (Map<String, Object> childSpan : childrenByParent.getOrDefault(spanId, List.of())) {
            if (service.equals(stringValue(childSpan.get("service")))) {
                total += serviceSubtreeError(childSpan, childrenByParent, service);
            }
        }
        return total;
    }

    private static Map<String, Object> findRootSpan(
            Map<String, Map<String, Object>> spanById,
            Map<String, List<Map<String, Object>>> childrenByParent) {
        for (Map<String, Object> span : spanById.values()) {
            String parentId = parentId(span);
            if (parentId.isBlank() || "0".equals(parentId) || !spanById.containsKey(parentId)) {
                return span;
            }
        }
        List<Map<String, Object>> roots = childrenByParent.getOrDefault("0", List.of());
        return roots.isEmpty() ? null : roots.get(0);
    }

    private static long traceDurationNs(List<Map<String, Object>> spans, Map<String, Object> rootSpan) {
        long minStart = Long.MAX_VALUE;
        long maxEnd = 0L;
        for (Map<String, Object> span : spans) {
            long startNs = longValue(span.get("startNs"));
            if (startNs <= 0L) {
                startNs = longValue(span.get("startTime")) * 1_000_000L;
            }
            long durationNs = longValue(span.get("duration"));
            if (startNs > 0L) {
                minStart = Math.min(minStart, startNs);
                maxEnd = Math.max(maxEnd, startNs + durationNs);
            }
        }
        if (minStart != Long.MAX_VALUE && maxEnd > minStart) {
            return maxEnd - minStart;
        }
        return longValue(rootSpan.get("duration"));
    }

    private static String parentId(Map<String, Object> span) {
        return stringValue(span.get("parent_id"));
    }

    private static String serviceId(Map<String, Object> span) {
        return PortalServiceIdResolver.resolve(
                stringValue(span.get("serviceId")),
                stringValue(span.get("service")));
    }

    private static double ratio(Object numerator, long denominator) {
        if (denominator == 0L) {
            return 0.0;
        }
        return BigDecimal.valueOf(longValue(numerator))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
