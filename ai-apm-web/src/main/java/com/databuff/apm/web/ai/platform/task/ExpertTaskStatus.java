package com.databuff.apm.web.ai.platform.task;

public enum ExpertTaskStatus {
    CREATED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    TIMEOUT,
    CANCELLED;

    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED || this == TIMEOUT || this == CANCELLED;
    }
}
