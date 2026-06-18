package com.databuff.apm.web.ai.platform.task;

import java.util.Map;

public record ExpertTaskRequest(
        String sessionId,
        String sourceExpertId,
        String targetExpertId,
        String input,
        String parentTaskId,
        Map<String, Object> metadata) {

    public ExpertTaskRequest {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
