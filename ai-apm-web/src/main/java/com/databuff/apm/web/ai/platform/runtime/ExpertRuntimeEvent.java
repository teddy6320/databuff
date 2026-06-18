package com.databuff.apm.web.ai.platform.runtime;

public record ExpertRuntimeEvent(
        String type,
        String content) {

    public static ExpertRuntimeEvent text(String content) {
        return new ExpertRuntimeEvent("text", content);
    }

    public static ExpertRuntimeEvent reasoning(String content) {
        return new ExpertRuntimeEvent("reasoning", content);
    }

    public static ExpertRuntimeEvent toolCall(String content) {
        return new ExpertRuntimeEvent("tool_call", content);
    }

    public static ExpertRuntimeEvent toolResult(String content) {
        return new ExpertRuntimeEvent("tool_result", content);
    }

    public static ExpertRuntimeEvent error(String content) {
        return new ExpertRuntimeEvent("error", content);
    }
}
