package com.databuff.apm.web.support;

import com.databuff.apm.common.cluster.coordination.ClusterInstanceCoordinator;
import com.databuff.apm.web.ai.agent.AiRuntimeRouter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class WebTestClusterSupport {

    private WebTestClusterSupport() {
    }

    public static AiRuntimeRouter standaloneAiRouter(String localNodeId) {
        ClusterInstanceCoordinator coordinator = mock(ClusterInstanceCoordinator.class);
        when(coordinator.localNodeId()).thenReturn(localNodeId);
        when(coordinator.effectiveClusterEnabled()).thenReturn(false);
        when(coordinator.sortedMembers()).thenReturn(List.of(localNodeId));
        when(coordinator.endpointsByNodeId()).thenReturn(Map.of());
        when(coordinator.partitionOwner(org.mockito.ArgumentMatchers.anyString())).thenReturn(localNodeId);
        when(coordinator.endpointFor(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());
        return new AiRuntimeRouter(coordinator, false);
    }
}
