package com.databuff.apm.web.portal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/globalTopology")
public class GlobalTopologyPortalController {

    private final GlobalTopologyPortalService globalTopologyPortalService;

    public GlobalTopologyPortalController(GlobalTopologyPortalService globalTopologyPortalService) {
        this.globalTopologyPortalService = globalTopologyPortalService;
    }

    @PostMapping("/graph")
    public Map<String, Object> graph(@RequestBody Map<String, Object> body) {
        return portalEnvelope(globalTopologyPortalService.graph(body));
    }

    @PostMapping("/verticalTree")
    public Map<String, Object> verticalTree(@RequestBody Map<String, Object> body) {
        return portalEnvelope(globalTopologyPortalService.verticalTree(body));
    }

    private static Map<String, Object> portalEnvelope(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
