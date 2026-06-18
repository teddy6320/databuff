package com.databuff.apm.web.monitor;

import com.databuff.apm.common.query.ApmQueryModels.MetricSeriesPoint;
import com.databuff.apm.common.query.TimeSeriesFillUtil;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.ai.TestBeanSupport;
import com.databuff.apm.web.monitor.policy.ResponsePolicyService;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.metric.MetricQueryService;
import com.databuff.apm.web.portal.MetricPortalService;
import com.databuff.apm.web.portal.EventPortalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * End-to-end check for monitor metric preview:
 * {@code EventPortalService.previewMonitorGraphV3} → {@code MetricPortalService.chart}
 * → {@code MetricQueryService.metricChart}.
 */
@SpringJUnitConfig(classes = MonitorPreviewGraphIntegrationTest.Config.class)
@ActiveProfiles({"local", "test"})
class MonitorPreviewGraphIntegrationTest {

    static final long START_MS = 1_780_906_620_000L;
    static final long END_MS = 1_780_910_220_000L;
    static final int INTERVAL_SEC = 60;
    static final long BUCKET_EPOCH_SEC = TimeSeriesFillUtil.alignBucketEpochSec(START_MS, INTERVAL_SEC);

    @Autowired
    private ApmReadRepository readRepository;

    @Autowired
    private EventPortalService eventPortalService;

    @BeforeEach
    void setUp() throws Exception {
        reset(readRepository);
    }

    @Test
    void previewMonitorGraphV3_returnsGroupedHttpCntSeries() throws Exception {
        when(readRepository.queryTopGroups(anyString())).thenReturn(List.of("demo-order", "demo-pay"));
        when(readRepository.queryMetricSeries(anyString())).thenReturn(List.of(
                new MetricSeriesPoint(BUCKET_EPOCH_SEC, 42.0)));

        Map<String, Object> response = eventPortalService.previewMonitorGraphV3(previewRequest());

        assertThat(response.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> series = (List<Map<String, Object>>) response.get("data");
        assertThat(series).hasSize(2);
        assertThat(series.get(0).get("tags")).isEqualTo(Map.of("service", "demo-order"));
        assertThat(series.get(1).get("tags")).isEqualTo(Map.of("service", "demo-pay"));

        @SuppressWarnings("unchecked")
        List<List<Number>> values = (List<List<Number>>) series.get(0).get("values");
        int expectedBuckets = TimeSeriesFillUtil.fillMetricSeries(
                List.of(new MetricSeriesPoint(BUCKET_EPOCH_SEC, 42.0)),
                START_MS,
                END_MS,
                INTERVAL_SEC).size();
        assertThat(values).hasSize(expectedBuckets);
        assertThat(values.get(0)).containsExactly(BUCKET_EPOCH_SEC * 1000L, 42.0);
        assertThat(values.get(1).get(0)).isEqualTo((BUCKET_EPOCH_SEC + INTERVAL_SEC) * 1000L);
        assertThat(values.get(1).get(1)).isNull();
    }

    @Test
    void previewMonitorGraphV3_queriesPerMinuteSeriesForEachGroup() throws Exception {
        when(readRepository.queryTopGroups(anyString())).thenReturn(List.of("demo-order"));
        when(readRepository.queryMetricSeries(anyString())).thenReturn(List.of(
                new MetricSeriesPoint(BUCKET_EPOCH_SEC, 12.0),
                new MetricSeriesPoint(BUCKET_EPOCH_SEC + INTERVAL_SEC, 18.0)));

        eventPortalService.previewMonitorGraphV3(previewRequest());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(readRepository).queryMetricSeries(sqlCaptor.capture());
        String seriesSql = sqlCaptor.getValue();
        assertThat(seriesSql).contains("metric_service_http");
        assertThat(seriesSql).contains("GROUP BY epoch_sec");
        assertThat(seriesSql).contains("ORDER BY epoch_sec ASC");
        assertThat(seriesSql).contains("`service` = 'demo-order'");
    }

    @Test
    void previewMonitorGraphV3_queriesMetricServiceHttpGroupedByService() throws Exception {
        when(readRepository.queryTopGroups(anyString())).thenReturn(List.of("demo-order"));
        when(readRepository.queryMetricSeries(anyString())).thenReturn(List.of(
                new MetricSeriesPoint(BUCKET_EPOCH_SEC, 10.0)));

        eventPortalService.previewMonitorGraphV3(previewRequest());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(readRepository).queryTopGroups(sqlCaptor.capture());
        String topSql = sqlCaptor.getValue();
        assertThat(topSql).contains("metric_service_http");
        assertThat(topSql).contains("GROUP BY `service`");
        assertThat(topSql).contains("`ts` >= " + START_MS);
        assertThat(topSql).contains("`ts` < " + END_MS);
        assertThat(topSql).contains("LIMIT 50");
    }

    @Test
    void previewMonitorGraphV3_returnsEmptyDataWhenNoGroups() throws Exception {
        when(readRepository.queryTopGroups(anyString())).thenReturn(List.of());

        Map<String, Object> response = eventPortalService.previewMonitorGraphV3(previewRequest());

        assertThat(response.get("status")).isEqualTo(200);
        assertThat(response.get("data")).isEqualTo(List.of());
    }

    @Test
    void previewMonitorGraphV3_returnsSingleSeriesWithoutGroupBy() throws Exception {
        when(readRepository.queryMetricSeries(anyString())).thenReturn(List.of(
                new MetricSeriesPoint(BUCKET_EPOCH_SEC, 99.0)));

        Map<String, Object> response = eventPortalService.previewMonitorGraphV3(previewRequestWithoutGroupBy());

        assertThat(response.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> series = (List<Map<String, Object>>) response.get("data");
        assertThat(series).hasSize(1);
        assertThat(series.get(0).get("tags")).isEqualTo(Map.of());

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(readRepository).queryMetricSeries(sqlCaptor.capture());
        String seriesSql = sqlCaptor.getValue();
        assertThat(seriesSql).contains("metric_service_http");
        assertThat(seriesSql).contains("GROUP BY epoch_sec");
        assertThat(seriesSql).doesNotContain("GROUP BY `service`");
    }

    private static Map<String, Object> previewRequestWithoutGroupBy() {
        return Map.of(
                "query", Map.of(
                        "A", Map.of(
                                "aggs", "sum",
                                "by", List.of(),
                                "metric", "service.http.cnt",
                                "from", List.of(),
                                "types", List.of()),
                        "expr", "A"),
                "start", START_MS,
                "end", END_MS,
                "interval", INTERVAL_SEC);
    }

    private static Map<String, Object> previewRequest() {
        return Map.of(
                "query", Map.of(
                        "A", Map.of(
                                "aggs", "sum",
                                "by", List.of("service"),
                                "metric", "service.http.cnt",
                                "from", List.of(),
                                "types", List.of()),
                        "expr", "A"),
                "start", START_MS,
                "end", END_MS,
                "interval", INTERVAL_SEC);
    }

    @Configuration
    static class Config {

        @Bean
        ApmReadRepository apmReadRepository() {
            return mock(ApmReadRepository.class);
        }

        @Bean
        ApmStorageProperties apmStorageProperties() {
            return TestStorageSupport.storage();
        }

        @Bean
        MetricQueryService metricQueryService(
                ApmReadRepository apmReadRepository,
                ApmStorageProperties apmStorageProperties) {
            return new MetricQueryService(apmReadRepository, apmStorageProperties);
        }

        @Bean
        MetricPortalService metricPortalService(MetricQueryService metricQueryService) {
            return new MetricPortalService(metricQueryService);
        }

        @Bean
        EventPortalService eventPortalService(MetricPortalService metricPortalService) {
            return new EventPortalService(
                    null,
                    null,
                    null,
                new ResponsePolicyService(),
                null,
                metricPortalService);
        }
    }
}
