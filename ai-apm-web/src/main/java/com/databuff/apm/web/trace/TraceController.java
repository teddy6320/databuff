package com.databuff.apm.web.trace;

import com.databuff.apm.common.query.ApmQueryModels.SpanSummary;
import com.databuff.apm.common.query.ApmQueryModels.SpanDetail;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/apm/trace")
public class TraceController {

    private final TraceQueryService traceQueryService;

    public TraceController(TraceQueryService traceQueryService) {
        this.traceQueryService = traceQueryService;
    }

    @PostMapping("/spanList")
    public List<SpanSummary> spanList(@RequestBody TraceQueryService.SpanListRequest request) {
        return traceQueryService.spanList(request);
    }

    @PostMapping("/serviceInstances")
    public List<String> serviceInstances(@RequestBody TraceQueryService.SpanListRequest request) {
        return traceQueryService.serviceInstances(request);
    }

    @PostMapping("/k8sNamespaces")
    public List<String> k8sNamespaces(@RequestBody TraceQueryService.SpanListRequest request) {
        return traceQueryService.k8sNamespaces(request);
    }

    @PostMapping("/serviceK8sNamespaces")
    public Map<String, String> serviceK8sNamespaces(@RequestBody TraceQueryService.SpanListRequest request) {
        return traceQueryService.serviceK8sNamespaces(request);
    }

    @PostMapping("/serviceInstanceCounts")
    public Map<String, Integer> serviceInstanceCounts(@RequestBody TraceQueryService.SpanListRequest request) {
        return traceQueryService.serviceInstanceCounts(request);
    }

    @PostMapping("/detail")
    public List<SpanDetail> traceDetail(@RequestBody TraceQueryService.TraceDetailRequest request) {
        return traceQueryService.traceDetail(request);
    }
}
