package com.databuff.apm.web.ai.agent;

public enum AiMessageType {
    USER,
    TEXT,
    REASONING,
    TOOL_CALL,
    TOOL_RESULT,
    SUBTASK,
    ERROR;

    public static AiMessageType fromRole(String role, String messageType) {
        if ("user".equalsIgnoreCase(role)) {
            return USER;
        }
        if (messageType != null && !messageType.isBlank()) {
            try {
                return AiMessageType.valueOf(messageType.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // fall through
            }
        }
        return TEXT;
    }
}
