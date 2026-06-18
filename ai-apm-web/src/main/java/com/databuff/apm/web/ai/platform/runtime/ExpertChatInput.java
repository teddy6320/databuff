package com.databuff.apm.web.ai.platform.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

public record ExpertChatInput(
        String message,
        String sessionId,
        String userName,
        String assistantMessageId,
        Map<String, Object> context) {

    public ExpertChatInput {
        context = context == null ? Map.of() : Map.copyOf(context);
    }

    public ExpertChatInput(String message, Map<String, Object> context) {
        this(message, null, null, null, context);
    }

    public static ExpertChatInput of(String message) {
        return new ExpertChatInput(message, null, null, null, Map.of());
    }

    public Map<String, Object> mergedContext() {
        Map<String, Object> merged = new LinkedHashMap<>(context);
        if (sessionId != null && !sessionId.isBlank()) {
            merged.putIfAbsent("sessionId", sessionId);
        }
        if (userName != null && !userName.isBlank()) {
            merged.putIfAbsent("userName", userName);
        }
        return Map.copyOf(merged);
    }
}
