package com.databuff.apm.common.util;

import java.util.regex.Pattern;

/** Normalizes portal {@code serviceId} to the 16-char MD5 key used in meta and metrics. */
public final class PortalServiceIdResolver {

    private static final Pattern SERVICE_KEY = Pattern.compile("^[0-9a-f]{16}$");

    private PortalServiceIdResolver() {
    }

    public static String normalize(String serviceIdOrName) {
        if (serviceIdOrName == null || serviceIdOrName.isBlank()) {
            return "";
        }
        String trimmed = serviceIdOrName.trim();
        if (SERVICE_KEY.matcher(trimmed).matches()) {
            return trimmed;
        }
        return ServiceKeyUtil.of(trimmed);
    }

    public static String resolve(String metricServiceId, String serviceName) {
        if (metricServiceId != null && !metricServiceId.isBlank()) {
            return normalize(metricServiceId);
        }
        return normalize(serviceName);
    }

    public static String resolve(String metricServiceId, String serviceName, String requestServiceId) {
        if (metricServiceId != null && !metricServiceId.isBlank()) {
            return normalize(metricServiceId);
        }
        if (requestServiceId != null && !requestServiceId.isBlank()) {
            return normalize(requestServiceId);
        }
        return normalize(serviceName);
    }

    public static boolean matches(String requestId, String candidate) {
        if (requestId == null || requestId.isBlank() || candidate == null || candidate.isBlank()) {
            return false;
        }
        if (requestId.equals(candidate)) {
            return true;
        }
        return normalize(requestId).equals(normalize(candidate));
    }
}
