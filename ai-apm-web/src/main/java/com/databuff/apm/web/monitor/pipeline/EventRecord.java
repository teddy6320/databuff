package com.databuff.apm.web.monitor.pipeline;

import java.time.Instant;

public record EventRecord(
        String id,
        long ruleId,
        String ruleName,
        String service,
        String detectionWay,
        String level,
        String status,
        String message,
        String groupKey,
        boolean silenced,
        Instant triggeredAt) {

    public static final String STATUS_TRIGGER = "trigger";
    public static final String STATUS_RECOVER = "recover";
    public static final String STATUS_NORMAL = "normal";

    public boolean isAbnormal() {
        return STATUS_TRIGGER.equals(status) && !silenced;
    }
}
