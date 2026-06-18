package com.databuff.apm.web.ai.agent;

import com.databuff.apm.common.cluster.coordination.ClusterInstanceCoordinator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Routes AI chat sessions across web cluster members using the shared live membership view.
 */
@Component
public class AiRuntimeRouter {

    private final ClusterInstanceCoordinator coordinator;
    private final boolean aiClusterEnabled;

    public AiRuntimeRouter(
            ClusterInstanceCoordinator coordinator,
            @Value("${apm.ai.cluster.enabled:false}") boolean aiClusterEnabled) {
        this.coordinator = coordinator;
        this.aiClusterEnabled = aiClusterEnabled;
    }

    public RouteDecision route(
            AgentBrainService.ChatRequest request,
            String sessionId,
            String expertId,
            String userId) {
        String routeKey = routeKey(request, sessionId, expertId, userId);
        if (!aiClusterEnabled || !coordinator.effectiveClusterEnabled()) {
            return RouteDecision.local(coordinator.localNodeId(), routeKey);
        }
        String ownerNodeId = coordinator.partitionOwner(routeKey);
        return new RouteDecision(routeKey, ownerNodeId, coordinator.localNodeId().equals(ownerNodeId));
    }

    public Optional<String> endpoint(String nodeId) {
        return coordinator.endpointFor(nodeId);
    }

    public String localNodeId() {
        return coordinator.localNodeId();
    }

    static String routeKey(
            AgentBrainService.ChatRequest request,
            String sessionId,
            String expertId,
            String userId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return "session:" + sessionId;
        }
        if (userId != null && !userId.isBlank()) {
            return "user:" + userId + ":expert:" + expertId;
        }
        String requestId = request == null || request.requestId() == null || request.requestId().isBlank()
                ? UUID.randomUUID().toString()
                : request.requestId();
        return "expert:" + expertId + ":request:" + requestId;
    }

    public record RouteDecision(String routeKey, String ownerNodeId, boolean localOwner) {

        public static RouteDecision local(String nodeId, String routeKey) {
            return new RouteDecision(routeKey, nodeId, true);
        }
    }
}
