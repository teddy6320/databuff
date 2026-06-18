package com.databuff.apm.web.portal;

import com.databuff.apm.web.config.common.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meta/conf")
public class MetaConfController {

    @GetMapping("/unit")
    public Map<String, Object> units() {
        List<Map<String, String>> items = List.of(
                unit("count", "个"),
                unit("percent", "%"),
                unit("ns", "纳秒"),
                unit("ms", "毫秒"),
                unit("byte", "字节"),
                unit("bps", "比特/秒"));
        return CommonResponse.ok(items);
    }

    private static Map<String, String> unit(String code, String name) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("code", code);
        row.put("name", name);
        return row;
    }
}
