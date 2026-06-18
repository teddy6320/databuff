package com.databuff.apm.web.portal;

import com.databuff.apm.web.config.common.CommonResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/respPolicy")
public class RespPolicyController {

    private final EventPortalService eventPortalService;

    public RespPolicyController(EventPortalService eventPortalService) {
        this.eventPortalService = eventPortalService;
    }

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> body) {
        return CommonResponse.ok(eventPortalService.searchResponsePolicies(body == null ? Map.of() : body));
    }

    @PostMapping("/find")
    public Map<String, Object> find(@RequestBody Map<String, Object> body) {
        return eventPortalService.findResponsePolicy(body);
    }

    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(eventPortalService.saveResponsePolicy(body));
    }

    @PostMapping("/delete")
    public Map<String, Object> delete(@RequestBody Map<String, Object> body) {
        eventPortalService.deleteResponsePolicies(body);
        return CommonResponse.ok(null);
    }

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestBody Map<String, Object> body) {
        eventPortalService.publishResponsePolicies(body);
        return CommonResponse.ok(null);
    }

    @PostMapping("/export")
    public void export(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletResponse response) throws IOException {
        eventPortalService.exportResponsePolicies(body == null ? Map.of() : body, response);
    }
}
