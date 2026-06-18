package com.databuff.apm.web.tools.local;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InspectToolsTest {

    @Test
    void detectAnomalyFindsLatestSpikeWithoutThreshold() {
        Map<String, Object> detection = InspectTools.detectAnomaly(List.of(10.0, 11.0, 9.0, 50.0));

        assertThat(detection).containsEntry("anomaly", true)
                .containsEntry("reason", "最新点相对历史均值存在明显突增");
    }

    @Test
    void detectAnomalyRequiresEnoughPoints() {
        Map<String, Object> detection = InspectTools.detectAnomaly(List.of(10.0, 50.0));

        assertThat(detection).containsEntry("anomaly", false)
                .containsEntry("reason", "数据点不足")
                .containsEntry("points", 2);
    }
}
