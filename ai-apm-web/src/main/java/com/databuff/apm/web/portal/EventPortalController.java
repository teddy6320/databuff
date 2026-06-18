package com.databuff.apm.web.portal;

import com.databuff.apm.web.config.common.CommonResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitor")
public class EventPortalController {

    private final EventPortalService eventPortalService;

    public EventPortalController(EventPortalService eventPortalService) {
        this.eventPortalService = eventPortalService;
    }

    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody(required = false) Map<String, Object> body) {
        return eventPortalService.searchRules(body == null ? Map.of() : body);
    }

    @PostMapping("/findMonitorEventV2")
    public Map<String, Object> findMonitorEventV2(@RequestBody Map<String, Object> body) {
        return eventPortalService.findMonitorEventV2(body);
    }

    @PostMapping("/findEventDetailV2")
    public Map<String, Object> findEventDetailV2(@RequestBody Map<String, Object> body) {
        return eventPortalService.findEventDetailV2(body);
    }

    @GetMapping("/monitorObjs")
    public Map<String, Object> monitorObjs() {
        return eventPortalService.monitorObjs();
    }

    @PostMapping("/previewMonitorGraphV3")
    public Map<String, Object> previewMonitorGraphV3(@RequestBody(required = false) Map<String, Object> body) {
        return eventPortalService.previewMonitorGraphV3(body == null ? Map.of() : body);
    }

    @PostMapping("/addMonitor")
    public Map<String, Object> addMonitor(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(eventPortalService.createRule(body));
    }

    @PostMapping("/editMonitor")
    public Map<String, Object> editMonitor(@RequestBody Map<String, Object> body) {
        return eventPortalService.editMonitor(body);
    }

    @PostMapping("/batchDelMonitor")
    public Map<String, Object> batchDelMonitor(@RequestBody List<Map<String, Object>> body) {
        return eventPortalService.batchDeleteRules(body);
    }

    @PostMapping("/getMonitorDetail")
    public Map<String, Object> getMonitorDetail(@RequestBody Map<String, Object> body) {
        return eventPortalService.getMonitorDetail(body);
    }

    @PutMapping("/enable/{enabled}")
    public Map<String, Object> toggleRuleEnable(
            @PathVariable boolean enabled,
            @RequestBody(required = false) List<Long> ids) {
        return eventPortalService.toggleRulesEnabled(ids == null ? List.of() : ids, enabled);
    }

    @PostMapping("/getEventChartMap")
    public Map<String, Object> getEventChartMap(@RequestBody Map<String, Object> body) {
        return eventPortalService.getEventChartMap(body == null ? Map.of() : body);
    }
}
