package com.databuff.apm.common.serde;

import com.databuff.apm.common.model.DcSpan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class DCSpanJsonDecoder {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private DCSpanJsonDecoder() {
    }

    public static DcSpan decode(byte[] bytes) throws IOException {
        return decode(bytes, false);
    }

    public static DcSpan decode(byte[] bytes, boolean ignoreMap) throws IOException {
        if (!ignoreMap) {
            return MAPPER.readValue(bytes, DcSpan.class);
        }
        JsonNode root = MAPPER.readTree(bytes);
        DcSpan span = new DcSpan();
        span.minutes = root.path("minutes").asLong();
        span.serviceId = text(root, "serviceId");
        span.resource = text(root, "resource");
        span.error = root.path("error").asInt();
        span.slow = root.path("slow").asInt();
        span.hours = root.path("hours").asLong();
        span.span_id = text(root, "span_id");
        span.startTime = text(root, "startTime");
        span.is_parent = root.path("is_parent").asInt();
        span.trace_id = text(root, "trace_id");
        span.parent_id = text(root, "parent_id");
        span.service = text(root, "service");
        span.serviceInstance = text(root, "serviceInstance");
        span.srcService = text(root, "srcService");
        span.srcServiceId = text(root, "srcServiceId");
        span.srcServiceInstance = text(root, "srcServiceInstance");
        span.dstService = text(root, "dstService");
        span.dstServiceId = text(root, "dstServiceId");
        span.dstServiceInstance = text(root, "dstServiceInstance");
        span.end = root.path("end").asLong();
        span.hostName = text(root, "hostName");
        span.type = text(root, "type");
        span.isIn = root.path("isIn").asInt();
        span.duration = root.path("duration").asLong();
        span.start = root.path("start").asLong();
        span.host_id = text(root, "host_id");
        span.name = text(root, "name");
        span.isOut = root.path("isOut").asInt();
        span.meta = text(root, "meta");
        span.metrics = text(root, "metrics");
        if (root.has("meta.http.status_code")) {
            span.metaHttpStatusCode = root.get("meta.http.status_code").asInt();
        }
        if (root.has("meta.http.method")) {
            span.metaHttpMethod = root.get("meta.http.method").asText();
        }
        if (root.has("meta.http.url")) {
            span.metaHttpUrl = root.get("meta.http.url").asText();
        }
        if (root.has("meta.error.type")) {
            span.metaErrorType = root.get("meta.error.type").asText();
        }
        if (root.has("meta.peer.hostname")) {
            span.metaPeerHostname = root.get("meta.peer.hostname").asText();
        }
        return span;
    }

    private static String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }
}
