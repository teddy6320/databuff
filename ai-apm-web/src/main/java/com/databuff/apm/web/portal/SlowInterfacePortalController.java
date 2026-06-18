package com.databuff.apm.web.portal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portal-compatible slow-interface APIs ({@code POST /webapi/slowInterface/*}).
 */
@RestController
@RequestMapping("/slowInterface")
public class SlowInterfacePortalController {

    private final ServicePortalService servicePortalService;

    public SlowInterfacePortalController(ServicePortalService servicePortalService) {
        this.servicePortalService = servicePortalService;
    }

    @PostMapping("/getResourceRelations")
    public Map<String, Object> getResourceRelations(@RequestBody Map<String, Object> body) {
        return portalEnvelope(servicePortalService.resourceRelation(body));
    }

    @PostMapping("/updateResourceAlias")
    public Map<String, Object> updateResourceAlias(@RequestBody Map<String, Object> body) {
        return portalEnvelope(Map.of());
    }

    private static Map<String, Object> portalEnvelope(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
