package com.databuff.apm.web.portal;

import com.databuff.apm.common.query.ApmQueryModels.MetricSeriesPoint;
import com.databuff.apm.common.query.TimeSeriesFillUtil;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.ai.TestBeanSupport;
import com.databuff.apm.web.monitor.policy.ResponsePolicyService;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.metric.MetricQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = EventChartMapIntegrationTest.Config.class)
@ActiveProfiles({"local", "test"})
class EventChartMapIntegrationTest {

    static final long START_MS = 1_780_906_620_000L;
    static final long END_MS = 1_780_910_220_000L;
    static final int INTERVAL_SEC = 60;
    static final long BUCKET_EPOCH_SEC = TimeSeriesFillUtil.alignBucketEpochSec(START_MS, INTERVAL_SEC);

    @Autowired
    private ApmReadRepository readRepository;

    @Autowired
    private EventPortalService eventPortalService;

    @BeforeEach
    void setUp() {
        reset(readRepository);
    }

    @Test
    void getEventChartMap_returnsSeriesWithThresholdColumns() throws Exception {
        when(readRepository.queryMetricSeries(anyString())).thenReturn(List.of(
                new MetricSeriesPoint(BUCKET_EPOCH_SEC, 6.0)));

        Map<String, Object> response = eventPortalService.getEventChartMap(eventChartRequest());

        assertThat(response.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        assertThat(data).containsKey("1:service.http.cnt");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> series = (List<Map<String, Object>>) data.get("1:service.http.cnt");
        assertThat(series).hasSize(1);
        assertThat(series.get(0).get("columns")).isEqualTo(List.of("time", "service.http.cnt", "critical"));

        @SuppressWarnings("unchecked")
        List<List<Number>> values = (List<List<Number>>) series.get(0).get("values");
        assertThat(values.get(0)).containsExactly(BUCKET_EPOCH_SEC * 1000L, 6.0, 1.0);
    }

    @Test
    void getEventChartMap_returnsEmptyWhenMetricMissing() {
        Map<String, Object> response = eventPortalService.getEventChartMap(Map.of(
                "1", Map.of("A", Map.of()),
                "start", START_MS,
                "end", END_MS,
                "interval", INTERVAL_SEC));

        assertThat(response.get("status")).isEqualTo(200);
        assertThat(response.get("data")).isEqualTo(Map.of());
    }

    private static Map<String, Object> eventChartRequest() {
        return Map.of(
                "1", Map.of(
                        "way", "threshold",
                        "comparison", ">",
                        "A", Map.of(
                                "metric", "service.http.cnt",
                                "aggs", "sum",
                                "by", List.of(),
                                "from", List.of(Map.of(
                                        "left", "service",
                                        "operator", "=",
                                        "right", "service-b",
                                        "connector", "AND"))),
                        "thresholds", Map.of(
                                "critical", Map.of("value", 1.0, "comparator", ">"))),
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
