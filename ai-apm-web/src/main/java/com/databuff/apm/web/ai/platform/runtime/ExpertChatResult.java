package com.databuff.apm.web.ai.platform.runtime;

public record ExpertChatResult(
        boolean ok,
        String content,
        String error) {

    public static ExpertChatResult ok(String content) {
        return new ExpertChatResult(true, content, null);
    }

    public static ExpertChatResult failed(String error) {
        return new ExpertChatResult(false, null, error);
    }
}
