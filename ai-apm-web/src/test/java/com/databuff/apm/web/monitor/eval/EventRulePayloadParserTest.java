package com.databuff.apm.web.monitor.eval;

import com.databuff.apm.web.monitor.EventRule;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EventRulePayloadParserTest {

    @Test
    void derivesServiceThresholdAndQueryFromBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("classification", "singleMetric");
        body.put("ruleName", "order error rate");
        body.put("query", Map.of(
                "1", Map.of(
                        "way", "threshold",
                        "period", 300,
                        "A", Map.of(
                                "metric", "databuff.service.error_rate",
                                "from", List.of(Map.of("type", "service", "value", "demo-order"))),
                        "thresholds", Map.of(
                                "critical", Map.of("value", 0.08, "comparator", ">")))));

        EventRulePayloadParser.DerivedFields derived = EventRulePayloadParser.derive(body);

        assertThat(derived.classify()).isEqualTo("singleMetric");
        assertThat(derived.service()).isEqualTo("demo-order");
        assertThat(derived.threshold()).isEqualTo(0.08);
        assertThat(derived.metric()).isEqualTo(EventRule.METRIC_ERROR_RATE);
        assertThat(derived.queryJson()).contains("demo-order");
    }

    @Test
    void sanitizesUnsupportedQueryFields() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", Map.of(
                "1", Map.of(
                        "way", "baseline",
                        "expr", "A+B",
                        "exprName", "custom metric",
                        "no_data_timeframe", 10,
                        "require_full_window", true,
                        "evaluation_delay", 5,
                        "A", Map.of(
                                "metric", "service.cnt",
                                "from", List.of(Map.of("left", "service", "right", "demo-order")),
                                "by", List.of("service")),
                        "B", Map.of(
                                "metric", "service.error.pct",
                                "by", List.of("service")),
                        "thresholds", Map.of(
                                "critical", Map.of("value", 1, "comparator", ">")))));

        EventRulePayloadParser.DerivedFields derived = EventRulePayloadParser.derive(body);

        assertThat(derived.detectionWay()).isEqualTo(EventRule.WAY_THRESHOLD);
        assertThat(derived.queryJson()).doesNotContain("expr", "exprName", "evaluation_delay", "require_full_window", "\"B\"");
    }

    @Test
    void extractsPrimaryGroupByFieldsFromPrimaryQueryItem() {
        Map<String, Object> primary = Map.of(
                "way", "threshold",
                "A", Map.of(
                        "metric", "service.cnt",
                        "by", List.of("service", "hostName")));

        assertThat(EventRulePayloadParser.extractPrimaryGroupByFields(primary))
                .containsExactly("service", "hostName");
    }

    @Test
    void extractsDistinctGroupByFieldsFromQuery() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", Map.of(
                "1", Map.of(
                        "way", "threshold",
                        "A", Map.of(
                                "metric", "service.cnt",
                                "by", List.of("service", "hostName")),
                        "B", Map.of(
                                "metric", "service.error.pct",
                                "by", List.of("service", "resource")))));

        EventRulePayloadParser.DerivedFields derived = EventRulePayloadParser.derive(body);

        assertThat(EventRulePayloadParser.extractGroupByFields(derived.queryJson()))
                .containsExactly("service", "hostName");
    }
}
