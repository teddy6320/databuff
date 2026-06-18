package com.databuff.apm.web.metric;

import com.databuff.apm.common.query.ApmQueryModels.HttpEndpointPoint;
import com.databuff.apm.common.query.ApmQueryModels.HttpLatencyBucketPoint;
import com.databuff.apm.common.query.ApmQueryModels.MetricSeriesPoint;
import com.databuff.apm.common.query.ApmQueryModels.ServiceMetricPoint;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/apm/metric")
public class MetricController {

    private final MetricQueryService metricQueryService;

    public MetricController(MetricQueryService metricQueryService) {
        this.metricQueryService = metricQueryService;
    }

    @PostMapping("/serviceSeries")
    public List<ServiceMetricPoint> serviceSeries(
            @RequestBody MetricQueryService.ServiceSeriesRequest request) {
        return metricQueryService.serviceSeries(request);
    }

    @PostMapping("/httpEndpoints")
    public List<HttpEndpointPoint> httpEndpoints(
            @RequestBody MetricQueryService.HttpQueryRequest request) {
        return metricQueryService.httpEndpoints(request);
    }

    @PostMapping("/httpLatency")
    public List<HttpLatencyBucketPoint> httpLatency(
            @RequestBody MetricQueryService.HttpQueryRequest request) {
        return metricQueryService.httpLatencyBuckets(request);
    }

    @PostMapping("/lastTags")
    public Map<String, List<String>> lastTags(@RequestBody MetricQueryService.LastTagsRequest request) {
        return metricQueryService.lastTags(request);
    }

    @PostMapping("/series")
    public List<MetricSeriesPoint> metricSeries(@RequestBody MetricQueryService.MetricSeriesRequest request) {
        return metricQueryService.metricSeries(request);
    }

    @PostMapping("/chart")
    public List<Map<String, Object>> metricChart(@RequestBody Map<String, Object> body) {
        return metricQueryService.metricChart(body);
    }
}
