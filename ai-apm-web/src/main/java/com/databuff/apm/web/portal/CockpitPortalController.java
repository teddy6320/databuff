package com.databuff.apm.web.portal;

import com.databuff.apm.web.cockpit.TrafficLightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/cockpit")
public class CockpitPortalController {

    private final CockpitPortalService cockpitPortalService;
    private final TrafficLightService trafficLightService;

    public CockpitPortalController(
            CockpitPortalService cockpitPortalService,
            TrafficLightService trafficLightService) {
        this.cockpitPortalService = cockpitPortalService;
        this.trafficLightService = trafficLightService;
    }

    @PostMapping("/trafficLight")
    public Map<String, Object> trafficLight(@RequestBody Map<String, Object> body) {
        return portalEnvelope(cockpitPortalService.servicesHealth(body));
    }

    @PostMapping("/entityData")
    public Map<String, Object> entityData(@RequestBody Map<String, Object> body) {
        return portalEnvelope(cockpitPortalService.entityData(body));
    }

    @PostMapping("/workbench/getEntityDataCount")
    public Map<String, Object> getEntityDataCount(@RequestBody Map<String, Object> body) {
        return portalEnvelope(cockpitPortalService.entityData(body));
    }

    @PostMapping("/workbench/getAlarmCount")
    public Map<String, Object> getAlarmCount(@RequestBody Map<String, Object> body) {
        return portalEnvelope(cockpitPortalService.getAlarmCount(body));
    }

    @PostMapping("/alarm/getEntityAlarmList")
    public Map<String, Object> getEntityAlarmList(@RequestBody Map<String, Object> body) {
        return portalEnvelope(cockpitPortalService.getEntityAlarmList(body));
    }

    @GetMapping("/getConfig")
    public Map<String, Object> getConfig(@RequestParam(required = false) String type) {
        return portalEnvelope(buildHealthConfigView(type));
    }

    @PostMapping("/setConfig")
    public Map<String, Object> setConfig(@RequestBody Map<String, Object> body) {
        Map<String, Object> updates = new LinkedHashMap<>(body);
        String resolvedType = stringValue(body.get("type"), "alarm");
        Object red = body.get("red");
        Object yellow = body.get("yellow");
        if (red != null) {
            if ("exception".equalsIgnoreCase(resolvedType)) {
                updates.put("exceptionRed", red);
            } else {
                updates.put("alarmRed", red);
            }
        }
        if (yellow != null) {
            if ("exception".equalsIgnoreCase(resolvedType)) {
                updates.put("exceptionYellow", yellow);
            } else {
                updates.put("alarmYellow", yellow);
            }
        }
        trafficLightService.setConfig(updates);
        return portalEnvelope(buildHealthConfigView(resolvedType));
    }

    private Map<String, Object> buildHealthConfigView(String type) {
        Map<String, Object> raw = trafficLightService.getConfig();
        Map<String, Object> config = new LinkedHashMap<>(raw);

        Map<String, Object> alarm = new LinkedHashMap<>();
        alarm.put("red", threshold(raw, "alarmRed", 2D));
        alarm.put("yellow", threshold(raw, "alarmYellow", 1D));

        Map<String, Object> exception = new LinkedHashMap<>();
        exception.put("red", threshold(raw, "exceptionRed", 10D));
        exception.put("yellow", threshold(raw, "exceptionYellow", 2D));

        config.put("alarm", alarm);
        config.put("exception", exception);
        config.putIfAbsent("showServiceNumber", 10);

        String resolvedType = stringValue(type, "alarm");
        config.put("type", resolvedType);
        if ("exception".equalsIgnoreCase(resolvedType)) {
            config.put("red", exception.get("red"));
            config.put("yellow", exception.get("yellow"));
        } else {
            config.put("red", alarm.get("red"));
            config.put("yellow", alarm.get("yellow"));
        }
        return config;
    }

    private static double threshold(Map<String, Object> config, String key, double fallback) {
        Object value = config.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    @PostMapping("/countServiceAlarms")
    public Map<String, Object> countServiceAlarms(@RequestBody Map<String, Object> body) {
        return portalEnvelope(cockpitPortalService.countServiceAlarms(body));
    }

    @PostMapping("/countServiceAlarmsTotal")
    public Map<String, Object> countServiceAlarmsTotal(@RequestBody Map<String, Object> body) {
        return portalEnvelope(cockpitPortalService.countServiceAlarmsTotal(body));
    }

    private static Map<String, Object> portalEnvelope(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }
}
