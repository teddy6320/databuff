package com.databuff.apm.web.cockpit;

import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.persistence.TrafficLightConfigPersistence;

import com.databuff.apm.common.query.ApmQueryModels.TrafficLightPoint;

import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CockpitControllerTest {

    @Test
    void delegatesToService() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryTrafficLight(anyString())).thenReturn(List.of(
                new TrafficLightPoint("2026-06-01 12:00:00", "checkout", 1, 5)));
        CockpitController controller = new CockpitController(
                new TrafficLightService(reader, TestStorageSupport.storage()),
                Mockito.mock(TrafficLightConfigPersistence.class));

        assertThat(controller.trafficLight(0, 1000)).hasSize(1);
        assertThat(controller.getConfig()).containsKey("errorRateThreshold");
        assertThat(controller.setConfig(Map.of("minRequestCount", 20)).get("minRequestCount")).isEqualTo(20);
    }
}
