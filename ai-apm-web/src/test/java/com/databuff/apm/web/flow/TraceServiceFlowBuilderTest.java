package com.databuff.apm.web.flow;

import com.databuff.apm.common.util.PortalServiceIdResolver;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TraceServiceFlowBuilderTest {

    @Test
    void buildsServiceTreeFromTraceSpans() {
        Map<String, Object> root = span("s1", "0", "gateway", "gw-id", 100_000_000L, 0);
        Map<String, Object> internal = span("s2", "s1", "gateway", "gw-id", 20_000_000L, 0);
        Map<String, Object> checkout = span("s3", "s2", "checkout", "co-id", 50_000_000L, 0);
        Map<String, Object> payment = span("s4", "s3", "payment", "pay-id", 30_000_000L, 1);

        Map<String, Object> flow = TraceServiceFlowBuilder.build(List.of(root, internal, checkout, payment));

        assertThat(flow).isNotEmpty();
        assertThat(flow.get("service")).isEqualTo("gateway");
        assertThat(flow.get("serviceId")).isEqualTo(
                PortalServiceIdResolver.resolve("gw-id", "gateway"));
        assertThat(flow.get("call")).isEqualTo(1L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) flow.get("children");
        assertThat(children).hasSize(1);
        assertThat(children.get(0).get("service")).isEqualTo("checkout");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checkoutChildren = (List<Map<String, Object>>) children.get(0).get("children");
        assertThat(checkoutChildren).hasSize(1);
        assertThat(checkoutChildren.get(0).get("service")).isEqualTo("payment");
        assertThat(checkoutChildren.get(0).get("error")).isEqualTo(1L);
    }

    @Test
    void returnsEmptyWhenSpansMissing() {
        assertThat(TraceServiceFlowBuilder.build(List.of())).isEmpty();
        assertThat(TraceServiceFlowBuilder.build(null)).isEmpty();
    }

    private static Map<String, Object> span(
            String spanId,
            String parentId,
            String service,
            String serviceId,
            long durationNs,
            long error) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("span_id", spanId);
        row.put("parent_id", parentId);
        row.put("service", service);
        row.put("serviceId", serviceId);
        row.put("duration", durationNs);
        row.put("error", error);
        row.put("startNs", 1_000_000_000L);
        row.put("hostName", "host-" + service);
        return row;
    }
}
