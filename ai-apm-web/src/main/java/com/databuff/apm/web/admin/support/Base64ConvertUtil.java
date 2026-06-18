package com.databuff.apm.web.admin.support;

import java.util.Base64;

/** Base64 helpers aligned with databuff-portal {@code Base64ConvertUtil}. */
public final class Base64ConvertUtil {

    private Base64ConvertUtil() {
    }

    public static String encode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static String decode(String str) {
        return new String(Base64.getDecoder().decode(str.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                java.nio.charset.StandardCharsets.UTF_8);
    }
}
