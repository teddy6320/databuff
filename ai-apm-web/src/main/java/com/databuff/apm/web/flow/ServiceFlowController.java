package com.databuff.apm.web.flow;

import com.databuff.apm.common.query.ApmQueryModels.ServiceFlowEdge;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/apm/serviceFlow")
public class ServiceFlowController {

    private final ServiceFlowService serviceFlowService;

    public ServiceFlowController(ServiceFlowService serviceFlowService) {
        this.serviceFlowService = serviceFlowService;
    }

    @PostMapping("/edges")
    public List<ServiceFlowEdge> edges(@RequestBody ServiceFlowRequest request) {
        return serviceFlowService.listFlows(
                request.service(),
                request.from(),
                request.to(),
                request.limit());
    }

    public record ServiceFlowRequest(String service, long from, long to, int limit) {
        public ServiceFlowRequest {
            if (limit <= 0) {
                limit = 100;
            }
        }
    }
}
