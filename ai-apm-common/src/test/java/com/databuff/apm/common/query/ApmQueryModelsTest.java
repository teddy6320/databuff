package com.databuff.apm.common.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApmQueryModelsTest {

    @Test
    void errorRateSnapshotComputesRate() {
        Assertions.assertThat(new ApmQueryModels.ErrorRateSnapshot(0, 0).errorRate()).isZero();
        Assertions.assertThat(new ApmQueryModels.ErrorRateSnapshot(2, 10).errorRate()).isEqualTo(0.2);
    }

    @Test
    void recordsExposeFields() {
        ApmQueryModels.TrafficLightPoint traffic = new ApmQueryModels.TrafficLightPoint("2024-01-01", "svc", 1, 10);
        assertThat(traffic.service()).isEqualTo("svc");

        ApmQueryModels.SpanSummary summary = new ApmQueryModels.SpanSummary(
                "t1", "s1", "svc", "svc-id", "op", "2024-01-01", 100, 0, "inst-1", "/api", "host-1", 500, "ServerError");
        assertThat(summary.traceId()).isEqualTo("t1");
        assertThat(summary.serviceInstance()).isEqualTo("inst-1");

        ApmQueryModels.SpanDetail detail = new ApmQueryModels.SpanDetail("t1", "s1", "p1", "svc", "svc-id", "op", "2024-01-01", 1_704_061_200_000_000_000L, 100, 1, "host");
        assertThat(detail.error()).isOne();

        ApmQueryModels.ServiceMetricPoint metric = new ApmQueryModels.ServiceMetricPoint("2024-01-01", "svc", 5, 1, 12.5);
        assertThat(metric.avgDuration()).isEqualTo(12.5);

        ApmQueryModels.TopologyEdge edge = new ApmQueryModels.TopologyEdge("a", "b", 3, 0);
        assertThat(edge.callCount()).isEqualTo(3);

        ApmQueryModels.ServiceFlowEdge flow = new ApmQueryModels.ServiceFlowEdge("a", "b", 3, 0, 9.0, null, null);
        assertThat(flow.avgDuration()).isEqualTo(9.0);
    }
}
