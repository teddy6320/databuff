package com.databuff.apm.web.cluster;

public final class WebNodeIds {

    private WebNodeIds() {
    }

    public static String resolve(String configuredNodeId, String hostname) {
        if (configuredNodeId != null && !configuredNodeId.isBlank() && !"auto".equalsIgnoreCase(configuredNodeId.trim())) {
            return configuredNodeId.trim();
        }
        if (hostname != null && !hostname.isBlank()) {
            return hostname.trim();
        }
        return "web-1";
    }
}
