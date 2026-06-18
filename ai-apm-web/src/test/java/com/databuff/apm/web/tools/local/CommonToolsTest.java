package com.databuff.apm.web.tools.local;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CommonToolsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CommonTools commonTools = new CommonTools(objectMapper);

    @Test
    void drawTrendChartsReturnsTrendBatchPayload() throws Exception {
        TrendChartSpec chart = new TrendChartSpec();
        chart.setTitle("服务平均响应时间");
        chart.setLabels(List.of("10:00", "10:01"));
        chart.setValues(List.of(12.0, 18.0));
        chart.setSeriesName("avgDuration");
        chart.setUnit("ms");

        Map<String, Object> payload = objectMapper.readValue(
                commonTools.drawTrendCharts(List.of(chart)),
                new TypeReference<>() {
                });

        assertThat(payload).containsEntry("chartType", "trendBatch")
                .containsEntry("success", true)
                .containsEntry("count", 1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> charts = (List<Map<String, Object>>) payload.get("charts");
        assertThat(charts).hasSize(1);
        assertThat(charts.get(0)).containsEntry("chartType", "trend")
                .containsEntry("title", "服务平均响应时间")
                .containsEntry("seriesName", "avgDuration")
                .containsEntry("unit", "ms");
    }

    @Test
    void drawTrendChartsRejectsEmptyInput() throws Exception {
        Map<String, Object> payload = objectMapper.readValue(
                commonTools.drawTrendCharts(List.of()),
                new TypeReference<>() {
                });

        assertThat(payload).containsEntry("success", false)
                .containsEntry("count", 0)
                .containsEntry("error", "charts is empty");
    }
}
