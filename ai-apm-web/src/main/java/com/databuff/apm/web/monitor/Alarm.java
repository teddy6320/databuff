package com.databuff.apm.web.monitor;

import java.time.Instant;

public record Alarm(
        String id,
        long policyId,
        String service,
        String detectionWay,
        String level,
        String message,
        String status,
        Instant triggeredAt,
        Instant resolvedAt) {

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_RESOLVED = "resolved";

    public Alarm resolve(Instant at) {
        return new Alarm(id, policyId, service, detectionWay, level, message, STATUS_RESOLVED, triggeredAt, at);
    }
}
