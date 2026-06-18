package com.databuff.apm.web.portal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/business")
public class BusinessPortalController {

    private final BusinessPortalService businessPortalService;

    public BusinessPortalController(BusinessPortalService businessPortalService) {
        this.businessPortalService = businessPortalService;
    }

    @PostMapping("/callInfo")
    public Map<String, Object> callInfo(@RequestBody Map<String, Object> body) {
        return portalEnvelope(businessPortalService.callInfo(body));
    }

    @PostMapping("/callEndpoints")
    public Map<String, Object> callEndpoints(@RequestBody Map<String, Object> body) {
        return businessPortalService.callEndpoints(body);
    }

    private static Map<String, Object> portalEnvelope(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
