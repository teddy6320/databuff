package com.databuff.apm.web.cockpit;

import com.databuff.apm.common.query.ApmQueryModels.TrafficLightPoint;

import com.databuff.apm.web.persistence.TrafficLightConfigPersistence;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cockpit")
public class CockpitController {

    private final TrafficLightService trafficLightService;
    private final TrafficLightConfigPersistence configSync;

    public CockpitController(TrafficLightService trafficLightService, TrafficLightConfigPersistence configSync) {
        this.trafficLightService = trafficLightService;
        this.configSync = configSync;
    }

    @PostMapping("/trafficLight")
    public List<TrafficLightPoint> trafficLight(
            @RequestParam long from,
            @RequestParam long to) {
        return trafficLightService.trafficLight(from, to);
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return trafficLightService.getConfig();
    }

    @PostMapping("/config")
    public Map<String, Object> setConfig(@RequestBody Map<String, Object> body) {
        trafficLightService.setConfig(body);
        configSync.persist(body);
        return trafficLightService.getConfig();
    }
}
