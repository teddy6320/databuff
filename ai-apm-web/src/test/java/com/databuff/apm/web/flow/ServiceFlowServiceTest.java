package com.databuff.apm.web.flow;

import com.databuff.apm.common.query.ApmQueryModels.ServiceFlowEdge;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.TestStorageSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceFlowServiceTest {

    @Test
    void returnsFlowEdgesFromStore() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryServiceFlow(anyString())).thenReturn(List.of(
                new ServiceFlowEdge("a", "b", 3, 0, 9.0, null, null)));

        ServiceFlowService service = new ServiceFlowService(reader, TestStorageSupport.storage());
        assertThat(service.listFlows("a", 0, 1000, 20)).hasSize(1);
    }

    @Test
    void returnsEmptyWhenStoreFails() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryServiceFlow(anyString())).thenThrow(new RuntimeException("down"));

        ServiceFlowService service = new ServiceFlowService(reader, TestStorageSupport.storage());
        assertThat(service.listFlows("a", 0, 1000, 20)).isEmpty();
    }
}
