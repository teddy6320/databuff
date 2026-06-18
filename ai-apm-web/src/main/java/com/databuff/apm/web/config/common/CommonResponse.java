package com.databuff.apm.web.config.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Portal-compatible JSON envelope ({@code status}/{@code message}/{@code data}). */
public final class CommonResponse {

    private CommonResponse() {
    }

    /**
     * Paginated list shape returned directly by portal list APIs (e.g. {@code POST /service/list}).
     * Matches legacy portal API shape ({@code ServiceV2ServiceImpl.prepareData}) + list pagination fields.
     */
    public static Map<String, Object> listPage(List<?> rows, long total, int offset, int returnedSize) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", rows);
        response.put("status", 200);
        response.put("message", "SUCCESS");
        response.put("total", total);
        response.put("size", returnedSize);
        response.put("offset", offset + returnedSize);
        return response;
    }

    public static Map<String, Object> emptyListPage() {
        return listPage(List.of(), 0, 0, 0);
    }

    public static Map<String, Object> ok(Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "success");
        response.put("data", data);
        return response;
    }

    public static Map<String, Object> listData(List<?> list, long total) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total);
        return ok(data);
    }

    public static Map<String, Object> emptyListData() {
        return listData(List.of(), 0);
    }

    public static Map<String, Object> fail(int status, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", status);
        response.put("message", message);
        return response;
    }
}
