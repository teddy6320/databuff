package com.databuff.apm.web.portal;

import com.databuff.apm.web.config.common.CommonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitor/system")
public class SystemEventPortalController {

    private final EventPortalService eventPortalService;

    public SystemEventPortalController(EventPortalService eventPortalService) {
        this.eventPortalService = eventPortalService;
    }

    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody(required = false) Map<String, Object> body) {
        return eventPortalService.searchSystemRules(body == null ? Map.of() : body);
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
