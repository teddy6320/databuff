package com.databuff.apm.web.admin.controller;

import com.databuff.apm.web.config.common.CommonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/saasCustomer")
public class SaasCustomerPortalController {

    /** Open-source build: account is never expired. */
    @PostMapping("/isOutTime")
    public Map<String, Object> isOutTime() {
        return CommonResponse.ok(0);
    }
}
