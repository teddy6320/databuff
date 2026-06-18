package com.databuff.apm.web.monitor.controller;

import com.databuff.apm.web.config.common.CommonResponse;
import com.databuff.apm.web.monitor.service.AlarmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @PostMapping("/count")
    public Map<String, Object> countAlarms(@RequestBody(required = false) Map<String, Object> body) {
        long count = alarmService.countAlarms(body == null ? Map.of() : body);
        return CommonResponse.ok(count);
    }

    @PostMapping("/queryParams")
    public Map<String, Object> queryParams(@RequestBody(required = false) Map<String, Object> body) {
        return alarmService.queryParams(body == null ? Map.of() : body);
    }

    @PostMapping("/trend")
    public Map<String, Object> trend(@RequestBody(required = false) Map<String, Object> body) {
        return alarmService.trend(body == null ? Map.of() : body);
    }

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> body) {
        return alarmService.list(body == null ? Map.of() : body);
    }

    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable String id) {
        return alarmService.detail(id);
    }

    @GetMapping("/detail/{alarmId}/trendMap/{interval}")
    public Map<String, Object> eventTrend(
            @PathVariable String alarmId,
            @PathVariable String interval) {
        return alarmService.eventTrend(alarmId, interval);
    }
}
