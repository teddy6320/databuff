package com.databuff.apm.web.ai.agent;

public enum AiMessageStatus {
    PENDING,
    STREAMING,
    COMPLETED,
    FAILED,
    TIMEOUT,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == TIMEOUT || this == CANCELLED;
    }
}
