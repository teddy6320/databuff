package com.databuff.apm.web.portal;

import com.databuff.apm.web.metric.MetricQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricPortalServiceTest {

    @Test
    void returnsDorisChartWhenAvailable() {
        MetricQueryService metricQueryService = mock(MetricQueryService.class);
        when(metricQueryService.metricChart(any())).thenReturn(List.of(
                Map.of("values", List.of(List.of(1_710_000_000L, 25L)), "tags", Map.of())));

        MetricPortalService service = new MetricPortalService(metricQueryService);
        List<Map<String, Object>> series = service.chart(Map.of(
                "start", 1_710_000_000L,
                "end", 1_710_003_600L,
                "query", Map.of("A", Map.of("metric", "service.thread.pool.poolSize"))));

        assertThat(series).hasSize(1);
    }
}
