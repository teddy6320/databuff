package com.databuff.apm.web.portal;

import com.databuff.apm.common.query.ApmQueryModels.ServiceFlowEdge;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalTopologyQueryServiceTest {

    @Test
    void prefersOutboundEdgeOverInboundForSamePair() {
        ServiceFlowEdge inbound = new ServiceFlowEdge("a", "b", 100, 1, 20.0, "a-id", "b-id");
        ServiceFlowEdge outbound = new ServiceFlowEdge("a", "b", 50, 0, 10.0, "a-id", "b-id");

        List<ServiceFlowEdge> merged = GlobalTopologyQueryService.mergePreferOutbound(
                List.of(inbound), List.of(outbound));

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).callCount()).isEqualTo(50);
        assertThat(merged.get(0).avgDuration()).isEqualTo(10.0);
    }

    @Test
    void keepsInboundWhenOutboundMissing() {
        ServiceFlowEdge inbound = new ServiceFlowEdge("a", "b", 100, 1, 20.0, "a-id", "b-id");

        List<ServiceFlowEdge> merged = GlobalTopologyQueryService.mergePreferOutbound(
                List.of(inbound), List.of());

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).callCount()).isEqualTo(100);
    }

    @Test
    void aggregatesDuplicateInboundEdges() {
        ServiceFlowEdge first = new ServiceFlowEdge("a", "b", 100, 1, 20.0, "a-id", "b-id");
        ServiceFlowEdge second = new ServiceFlowEdge("a", "b", 50, 1, 10.0, "a-id", "b-id");

        List<ServiceFlowEdge> merged = GlobalTopologyQueryService.mergePreferOutbound(
                List.of(first, second), List.of());

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).callCount()).isEqualTo(150);
        assertThat(merged.get(0).errorCount()).isEqualTo(2);
        assertThat(merged.get(0).avgDuration()).isCloseTo(16.666, org.assertj.core.data.Offset.offset(0.01));
    }
}
