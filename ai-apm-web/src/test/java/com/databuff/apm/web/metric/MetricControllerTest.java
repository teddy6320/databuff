package com.databuff.apm.web.metric;

import com.databuff.apm.web.TestStorageSupport;

import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MetricControllerTest {

    @Test
    void delegatesServiceSeries() {
        MetricController controller = new MetricController(
                new MetricQueryService(mock(ApmReadRepository.class), TestStorageSupport.storage()));
        assertThat(controller.serviceSeries(new MetricQueryService.ServiceSeriesRequest("demo", 0, 1))).isEmpty();
        assertThat(controller.httpEndpoints(new MetricQueryService.HttpQueryRequest("demo", 0, 1, 20))).isEmpty();
        assertThat(controller.httpLatency(new MetricQueryService.HttpQueryRequest("demo", 0, 1, 20))).isEmpty();
        assertThat(controller.lastTags(new MetricQueryService.LastTagsRequest(
                0, 1, java.util.List.of("service.object.pool.size"), java.util.List.of("objectPoolName"), java.util.List.of())))
                .containsEntry("objectPoolName", java.util.List.of());
        assertThat(controller.metricSeries(new MetricQueryService.MetricSeriesRequest(
                "service.object.pool.size", 0, 1, java.util.List.of())))
                .isEmpty();
        assertThat(controller.metricChart(java.util.Map.of())).isEmpty();
        List<java.util.Map<String, Object>> jvmChart = controller.metricChart(java.util.Map.of(
                "start", 0L,
                "end", 1L,
                "query", java.util.Map.of("A", java.util.Map.of("metric", "jvm.thread_count"))));
        assertThat(jvmChart).hasSize(1);
        assertThat(jvmChart.get(0).get("values")).isEqualTo(java.util.List.of());
        assertThat(controller.metricChart(java.util.Map.of(
                "start", 0L,
                "end", 1L,
                "query", java.util.Map.of("A", java.util.Map.of(
                        "metric", "service.thread.pool.poolSize",
                        "by", java.util.List.of("serviceInstance"),
                        "order", java.util.Map.of("limit", 5))))))
                .isEmpty();
    }
}
