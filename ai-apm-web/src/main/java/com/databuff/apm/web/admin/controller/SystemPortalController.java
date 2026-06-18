package com.databuff.apm.web.admin.controller;

import com.databuff.apm.web.config.common.CommonResponse;
import com.databuff.apm.web.admin.settings.DisplayLocaleSettingsService;
import com.databuff.apm.web.admin.settings.SessionIdleSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemPortalController {

    private final SessionIdleSettingsService sessionIdleSettingsService;
    private final DisplayLocaleSettingsService displayLocaleSettingsService;

    public SystemPortalController(
            SessionIdleSettingsService sessionIdleSettingsService,
            DisplayLocaleSettingsService displayLocaleSettingsService) {
        this.sessionIdleSettingsService = sessionIdleSettingsService;
        this.displayLocaleSettingsService = displayLocaleSettingsService;
    }

    /** Portal login bootstrap: empty map falls back to built-in logo defaults on the client. */
    @GetMapping("/logoConfig")
    public Map<String, Object> logoConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("locale", displayLocaleSettingsService.locale());
        return CommonResponse.ok(data);
    }

    @GetMapping("/getDate")
    public Map<String, Object> getDate() {
        return CommonResponse.ok(Instant.now().toEpochMilli());
    }

    @GetMapping("/systemBase")
    public Map<String, Object> systemBase() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("pageTimeOut", sessionIdleSettingsService.idleSeconds());
        data.put("locale", displayLocaleSettingsService.locale());
        data.put("ntpAuto", 0);
        data.put("ntpServer", "");
        return CommonResponse.ok(data);
    }

    @PostMapping("/updateDisplayLocale")
    public Map<String, Object> updateDisplayLocale(@RequestBody Map<String, Object> body) {
        String locale = stringValue(body == null ? null : body.get("locale"));
        if (!"zh-CN".equals(locale) && !"en-US".equals(locale)) {
            return CommonResponse.fail(400, "请选择有效的显示语言");
        }
        displayLocaleSettingsService.updateLocale(locale);
        return CommonResponse.ok(null);
    }

    @PostMapping("/updatePageTimeOut")
    public Map<String, Object> updatePageTimeOut(@RequestBody Map<String, Object> body) {
        int pageTimeOut = intValue(body == null ? null : body.get("pageTimeOut"));
        if (pageTimeOut <= 0) {
            return CommonResponse.fail(400, "请选择有效的超时时间");
        }
        sessionIdleSettingsService.updateIdleSeconds(pageTimeOut);
        return CommonResponse.ok(null);
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value).trim());
    }
}
