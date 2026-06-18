package com.databuff.apm.web.admin.controller;

import com.databuff.apm.web.config.common.CommonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/group")
public class GroupController {

    @PostMapping("/list")
    public Map<String, Object> list(@RequestBody(required = false) Map<String, Object> body) {
        return CommonResponse.listData(java.util.List.of(), 0);
    }

    @PostMapping("/status")
    public Map<String, Object> status(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", false);
        return CommonResponse.ok(data);
    }
}
