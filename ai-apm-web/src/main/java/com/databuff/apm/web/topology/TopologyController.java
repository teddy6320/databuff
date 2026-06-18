package com.databuff.apm.web.topology;

import com.databuff.apm.common.query.ApmQueryModels.TopologyEdge;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apm/topology")
public class TopologyController {

    private final TopologyQueryService topologyQueryService;

    public TopologyController(TopologyQueryService topologyQueryService) {
        this.topologyQueryService = topologyQueryService;
    }

    @PostMapping("/edges")
    public List<TopologyEdge> edges(@RequestBody TopologyQueryService.TopologyRequest request) {
        return topologyQueryService.serviceEdges(request);
    }
}
