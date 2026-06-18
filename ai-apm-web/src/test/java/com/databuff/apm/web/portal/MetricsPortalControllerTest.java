package com.databuff.apm.web.portal;

import com.databuff.apm.web.TestMetricCoreSupport;
import com.databuff.apm.common.metric.MetricQueryDefinition;
import com.databuff.apm.web.metric.MetricCoreCatalogService;
import com.databuff.apm.web.metric.MetricQueryService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricsPortalControllerTest {

    @Test
    void exploreMetricByGroupGraphWrapsFlattenedRequest() {
        MetricQueryService metricQueryService = mock(MetricQueryService.class);
        when(metricQueryService.metricChart(any())).thenReturn(List.of(
                Map.of("values", List.of(List.of(1_710_000_000L, 3L)), "tags", Map.of())));

        MetricsPortalController controller = new MetricsPortalController(metricQueryService, new MetricCoreCatalogService());
        Map<String, Object> resp = controller.exploreMetricByGroupGraph(Map.of(
                "start", 1_710_000_000L,
                "end", 1_710_003_600L,
                "interval", 60,
                "metric", "service.exception.cnt",
                "from", List.of(Map.of(
                        "left", "serviceId",
                        "operator", "=",
                        "right", "demo-order",
                        "connector", "AND"))));

        assertThat(resp.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) resp.get("data");
        assertThat(data).hasSize(1);
    }

    @Test
    void listTagValuesUsesMetricsArrayAndCatalogTagKeys() {
        MetricQueryService metricQueryService = mock(MetricQueryService.class);
        when(metricQueryService.lastTags(any())).thenReturn(Map.of(
                "serviceId", List.of("demo-order"),
                "serviceInstance", List.of("inst-1")));

        MetricCoreCatalogService catalogService = mock(MetricCoreCatalogService.class);
        MetricQueryDefinition definition = new MetricQueryDefinition();
        definition.setTagKey(new LinkedHashMap<>(Map.of(
                "serviceId", "服务",
                "serviceInstance", "服务实例")));
        when(catalogService.findOpenByIdentifier("service.request.count")).thenReturn(definition);

        MetricsPortalController controller = new MetricsPortalController(metricQueryService, catalogService);
        Map<String, Object> resp = controller.listTagValues(Map.of(
                "metrics", List.of("service.request.count"),
                "by", List.of(),
                "from", List.of()));

        assertThat(resp.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        assertThat(data).containsKeys("serviceId", "serviceInstance");
    }

    @Test
    void allTagKeysExposesGroupByDimensionsFromMetricCatalog() {
        MetricsPortalController controller = new MetricsPortalController(
                mock(MetricQueryService.class),
                TestMetricCoreSupport.catalogWithServiceMetrics());

        Map<String, Object> resp = controller.allTagKeys();

        assertThat(resp.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) resp.get("data");
        assertThat(data).containsKeys("service", "serviceId", "serviceInstance", "errorType");
        assertThat(data.get("service")).containsEntry("enabled", true);
        assertThat(data.get("service")).containsEntry("name", "服务名称");
    }

    @Test
    void searchAllMetricsReturnsDefinitionsByType() {
        MetricsPortalController controller = new MetricsPortalController(
                mock(MetricQueryService.class),
                TestMetricCoreSupport.catalogWithServiceMetrics());

        Map<String, Object> resp = controller.searchAllMetrics(Map.of(
                "type1", "应用性能",
                "type2", "入口请求",
                "type3", "请求总览"));

        assertThat(resp.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, MetricQueryDefinition> data = (Map<String, MetricQueryDefinition>) resp.get("data");
        assertThat(data).containsKey("service.cnt");
        assertThat(data.get("service.cnt").getType3()).isEqualTo("请求总览");
    }
}
