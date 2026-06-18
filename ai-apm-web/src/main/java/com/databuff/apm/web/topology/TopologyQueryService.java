package com.databuff.apm.web.topology;

import com.databuff.apm.common.query.ApmQueryModels.TopologyEdge;
import com.databuff.apm.web.portal.GlobalTopologyQueryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TopologyQueryService {

    private final GlobalTopologyQueryService globalTopologyQueryService;

    public TopologyQueryService(GlobalTopologyQueryService globalTopologyQueryService) {
        this.globalTopologyQueryService = globalTopologyQueryService;
    }

    public List<TopologyEdge> serviceEdges(TopologyRequest request) {
        try {
            return globalTopologyQueryService.listTopologyEdges(
                    request.from(), request.to(), request.limit());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public record TopologyRequest(long from, long to, int limit) {
        public TopologyRequest {
            if (limit <= 0) {
                limit = 100;
            }
        }
    }
}
