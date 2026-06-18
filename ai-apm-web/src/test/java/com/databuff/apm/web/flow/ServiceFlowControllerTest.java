package com.databuff.apm.web.flow;

import com.databuff.apm.web.TestStorageSupport;

import com.databuff.apm.common.query.ApmQueryModels.ServiceFlowEdge;

import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceFlowControllerTest {

    @Test
    void appliesDefaultLimit() {
        assertThat(new ServiceFlowController.ServiceFlowRequest("svc", 0, 1, 0).limit()).isEqualTo(100);
    }

    @Test
    void returnsFlowEdges() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryServiceFlow(anyString())).thenReturn(List.of(
                new ServiceFlowEdge("gateway", "checkout", 10, 1, 12.5, null, null)));

        ServiceFlowController controller = new ServiceFlowController(new ServiceFlowService(reader, TestStorageSupport.storage()));
        assertThat(controller.edges(new ServiceFlowController.ServiceFlowRequest("checkout", 0, 1000, 50)))
                .hasSize(1);
    }
}
