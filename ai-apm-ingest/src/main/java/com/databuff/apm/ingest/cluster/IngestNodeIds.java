package com.databuff.apm.ingest.cluster;

/**
 * Resolves ingest node id; {@code auto} maps StatefulSet pod ordinal to ingest-1, ingest-2, …,
 * or uses the pod hostname for Deployment-style names (e.g. ai-apm-ingest-7d4f8b9c5-abcde).
 */
public final class IngestNodeIds {

    private IngestNodeIds() {
    }

    public static String resolve(String configuredNodeId, String hostname) {
        if (configuredNodeId == null || configuredNodeId.isBlank() || "auto".equalsIgnoreCase(configuredNodeId)) {
            return fromHostname(hostname, "ingest-1");
        }
        return configuredNodeId;
    }

    static String fromHostname(String hostname, String fallback) {
        if (hostname == null || hostname.isBlank()) {
            return fallback;
        }
        int dash = hostname.lastIndexOf('-');
        if (dash >= 0 && dash < hostname.length() - 1) {
            try {
                int ordinal = Integer.parseInt(hostname.substring(dash + 1));
                return "ingest-" + (ordinal + 1);
            } catch (NumberFormatException ignored) {
                // Deployment-style pod name: hostname is already unique per pod.
            }
        }
        return hostname.trim();
    }
}
