package com.databuff.apm.common.serde;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class DCTraceUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DCTraceUtil() {
    }

    public static String getTraceId(byte[] spanBytes) throws IOException {
        JsonNode root = MAPPER.readTree(spanBytes);
        JsonNode traceId = root.get("trace_id");
        return traceId == null || traceId.isNull() ? "" : traceId.asText();
    }

    public static List<byte[]> spansFromPackage(byte[] packageBytes) throws IOException {
        JsonNode root = MAPPER.readTree(packageBytes);
        if (root.isArray()) {
            List<byte[]> spans = new ArrayList<>(root.size());
            for (JsonNode node : root) {
                spans.add(MAPPER.writeValueAsBytes(node));
            }
            return spans;
        }
        return List.of(packageBytes);
    }
}
