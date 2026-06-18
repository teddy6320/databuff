package com.databuff.apm.web.portal;

import com.databuff.apm.web.config.common.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/notify")
public class NotifyPortalController {

    @GetMapping("/getEmailConfig")
    public Map<String, Object> getEmailConfig() {
        return CommonResponse.ok(disabledChannel("mail"));
    }

    @PostMapping("/setEmailConfig")
    public Map<String, Object> setEmailConfig(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(null);
    }

    @PostMapping("/testEmail")
    public Map<String, Object> testEmail(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @GetMapping("/getSmsConfig")
    public Map<String, Object> getSmsConfig() {
        return CommonResponse.ok(disabledChannel("sms"));
    }

    @PostMapping("/setSmsConfig")
    public Map<String, Object> setSmsConfig(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(null);
    }

    @PostMapping("/testSms")
    public Map<String, Object> testSms(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @GetMapping("/getDingTalkConfig")
    public Map<String, Object> getDingTalkConfig() {
        return CommonResponse.ok(disabledChannel("dingtalk"));
    }

    @PostMapping("/setDingTalkConfig")
    public Map<String, Object> setDingTalkConfig(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(null);
    }

    @PostMapping("/testDingTalk")
    public Map<String, Object> testDingTalk(@RequestBody(required = false) Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @PostMapping("/testDingTalkByPhone")
    public Map<String, Object> testDingTalkByPhone(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @PostMapping("/testWeChatByPhone")
    public Map<String, Object> testWeChatByPhone(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @GetMapping("/getWeChatConfig")
    public Map<String, Object> getWeChatConfig() {
        return CommonResponse.ok(disabledChannel("wechat"));
    }

    @PostMapping("/setWeChatConfig")
    public Map<String, Object> setWeChatConfig(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(null);
    }

    @PostMapping("/testWeChat")
    public Map<String, Object> testWeChat(@RequestBody(required = false) Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @PostMapping("/testCustomWebhook")
    public Map<String, Object> testCustomWebhook(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @GetMapping("/getSocketConfig")
    public Map<String, Object> getSocketConfig() {
        return CommonResponse.ok(disabledChannel("socket"));
    }

    @PostMapping("/setSocketConfig")
    public Map<String, Object> setSocketConfig(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(null);
    }

    @PostMapping("/testSocket")
    public Map<String, Object> testSocket(@RequestBody(required = false) Map<String, Object> body) {
        return CommonResponse.ok("test skipped in open-source mode");
    }

    @PostMapping("/records")
    public Map<String, Object> records(@RequestBody(required = false) Map<String, Object> body) {
        return CommonResponse.emptyListData();
    }

    @PostMapping("/resend")
    public Map<String, Object> resend(@RequestBody Map<String, Object> body) {
        return CommonResponse.ok(null);
    }

    private static Map<String, Object> disabledChannel(String notifyType) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("notifyType", notifyType);
        config.put("enable", 0);
        return config;
    }
}
