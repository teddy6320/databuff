package com.databuff.apm.web.portal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/metric")
public class MetricPortalController {

    private final MetricPortalService metricPortalService;

    public MetricPortalController(MetricPortalService metricPortalService) {
        this.metricPortalService = metricPortalService;
    }

    @PostMapping("/chart")
    public Map<String, Object> chart(@RequestBody Map<String, Object> body) {
        return portalEnvelope(metricPortalService.chart(body));
    }

    @PostMapping("/lastTags")
    public Map<String, Object> lastTags(@RequestBody Map<String, Object> body) {
        return portalEnvelope(metricPortalService.lastTags(body));
    }

    @PostMapping("/serviceSeries")
    public Map<String, Object> serviceSeries(@RequestBody Map<String, Object> body) {
        return portalEnvelope(metricPortalService.serviceSeries(body));
    }

    @PostMapping("/httpEndpoints")
    public Map<String, Object> httpEndpoints(@RequestBody Map<String, Object> body) {
        return portalEnvelope(metricPortalService.httpEndpoints(body));
    }

    @PostMapping("/httpLatency")
    public Map<String, Object> httpLatency(@RequestBody Map<String, Object> body) {
        return portalEnvelope(metricPortalService.httpLatency(body));
    }

    @PostMapping("/series")
    public Map<String, Object> series(@RequestBody Map<String, Object> body) {
        return portalEnvelope(metricPortalService.metricSeries(body));
    }

    private static Map<String, Object> portalEnvelope(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
