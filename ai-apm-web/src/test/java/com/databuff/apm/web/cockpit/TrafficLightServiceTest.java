package com.databuff.apm.web.cockpit;

import com.databuff.apm.web.TestStorageSupport;

import com.databuff.apm.common.query.ApmQueryModels.TrafficLightPoint;

import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrafficLightServiceTest {

    @Test
    void returnsEmptyListWhenQueryFails() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryTrafficLight(anyString())).thenThrow(new RuntimeException("down"));

        TrafficLightService service = new TrafficLightService(reader, TestStorageSupport.storage());
        assertThat(service.trafficLight(0, 1000)).isEmpty();
    }

    @Test
    void updatesConfig() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryTrafficLight(anyString())).thenReturn(List.of(
                new TrafficLightPoint("2026-06-01 12:00:00", "checkout", 1, 10)));

        TrafficLightService service = new TrafficLightService(reader, TestStorageSupport.storage());
        assertThat(service.trafficLight(0, 1000)).hasSize(1);
        service.setConfig(java.util.Map.of("errorRateThreshold", 0.1));
        assertThat(service.getConfig().get("errorRateThreshold")).isEqualTo(0.1);
    }

    @Test
    void exposesDefaultHealthThresholds() {
        TrafficLightService service = new TrafficLightService(mock(ApmReadRepository.class), TestStorageSupport.storage());
        assertThat(service.getConfig())
                .containsEntry("alarmRed", 2)
                .containsEntry("alarmYellow", 1)
                .containsEntry("exceptionRed", 10)
                .containsEntry("exceptionYellow", 2);
    }
}
