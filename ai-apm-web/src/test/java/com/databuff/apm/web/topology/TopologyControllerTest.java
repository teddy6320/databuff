package com.databuff.apm.web.topology;

import com.databuff.apm.web.portal.GlobalTopologyQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TopologyControllerTest {

    @Test
    void delegatesEdgesQuery() {
        TopologyController controller = new TopologyController(
                new TopologyQueryService(Mockito.mock(GlobalTopologyQueryService.class)));
        assertThat(controller.edges(new TopologyQueryService.TopologyRequest(0, 1, 10))).isEmpty();
    }
}
