package com.databuff.apm.web.ai.agent;

public class AiPlatformChatException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public AiPlatformChatException(String errorCode, int httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String errorCode() {
        return errorCode;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public static AiPlatformChatException expertNotFound(String expertId) {
        return new AiPlatformChatException(
                "expert_not_found", 404, "expert not found: " + expertId);
    }

    public static AiPlatformChatException expertDisabled(String expertId) {
        return new AiPlatformChatException(
                "expert_disabled", 409, "expert is disabled: " + expertId);
    }

    public static AiPlatformChatException messageRequired() {
        return new AiPlatformChatException("message_required", 400, "message is required");
    }

    public static AiPlatformChatException taskNotFound(String taskId) {
        return new AiPlatformChatException("task_not_found", 404, "task not found: " + taskId);
    }

    public static AiPlatformChatException runtimeUnavailable(String expertId) {
        return new AiPlatformChatException(
                "runtime_unavailable",
                503,
                "AgentScope runtime unavailable for expert: " + expertId);
    }

    public static AiPlatformChatException sessionNotFound(String sessionId) {
        return new AiPlatformChatException(
                "session_not_found", 404, "session not found: " + sessionId);
    }
}
