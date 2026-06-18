package com.databuff.apm.web.ai.platform.task;

public record ExpertTaskCompletionEvent(
        String sessionId,
        int roundIndex,
        String taskId,
        String targetExpertId,
        String sourceExpertId,
        String userName,
        String text,
        boolean failure) {

    public static ExpertTaskCompletionEvent success(
            String sessionId,
            int roundIndex,
            String taskId,
            String targetExpertId,
            String sourceExpertId,
            String userName,
            String text) {
        return new ExpertTaskCompletionEvent(
                sessionId, roundIndex, taskId, targetExpertId, sourceExpertId, userName, text, false);
    }

    public static ExpertTaskCompletionEvent failure(
            String sessionId,
            int roundIndex,
            String taskId,
            String targetExpertId,
            String sourceExpertId,
            String userName,
            String error) {
        return new ExpertTaskCompletionEvent(
                sessionId,
                roundIndex,
                taskId,
                targetExpertId,
                sourceExpertId,
                userName,
                error == null ? "task failed" : error,
                true);
    }
}
