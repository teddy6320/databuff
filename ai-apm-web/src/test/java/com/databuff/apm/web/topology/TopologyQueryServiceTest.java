package com.databuff.apm.web.topology;

import com.databuff.apm.common.query.ApmQueryModels.TopologyEdge;
import com.databuff.apm.web.portal.GlobalTopologyQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TopologyQueryServiceTest {

    @Test
    void returnsEdgesFromDoris() {
        GlobalTopologyQueryService topology = mock(GlobalTopologyQueryService.class);
        when(topology.listTopologyEdges(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(
                        new TopologyEdge("gateway", "checkout", 10, 1),
                        new TopologyEdge("order", "[mysql]demo", 5, 0)));
        TopologyQueryService service = new TopologyQueryService(topology);
        assertThat(service.serviceEdges(new TopologyQueryService.TopologyRequest(0, 1000, 20))).hasSize(2);
    }

    @Test
    void returnsEmptyWhenDorisFails() {
        GlobalTopologyQueryService topology = mock(GlobalTopologyQueryService.class);
        when(topology.listTopologyEdges(anyLong(), anyLong(), anyInt())).thenThrow(new RuntimeException("down"));
        TopologyQueryService service = new TopologyQueryService(topology);
        assertThat(service.serviceEdges(new TopologyQueryService.TopologyRequest(0, 1000, 20))).isEmpty();
    }

    @Test
    void appliesDefaultLimit() {
        GlobalTopologyQueryService topology = mock(GlobalTopologyQueryService.class);
        when(topology.listTopologyEdges(anyLong(), anyLong(), anyInt())).thenReturn(List.of());
        TopologyQueryService service = new TopologyQueryService(topology);
        assertThat(service.serviceEdges(new TopologyQueryService.TopologyRequest(0, 1000, 0))).isEmpty();
    }
}
