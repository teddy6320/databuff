package com.databuff.apm.web.ai.platform.task;

public record ExpertTaskEvent(
        String type,
        String taskId,
        String targetExpertId,
        String payload) {

    public static ExpertTaskEvent created(ExpertTask task) {
        return new ExpertTaskEvent(
                "subtask.created",
                task.taskId(),
                task.targetExpertId(),
                task.input());
    }

    public static ExpertTaskEvent running(ExpertTask task) {
        return new ExpertTaskEvent("subtask.running", task.taskId(), task.targetExpertId(), task.status().name());
    }

    public static ExpertTaskEvent completed(ExpertTask task) {
        return new ExpertTaskEvent(
                "subtask.completed",
                task.taskId(),
                task.targetExpertId(),
                task.output());
    }

    public static ExpertTaskEvent failed(ExpertTask task) {
        return new ExpertTaskEvent(
                "subtask.failed",
                task.taskId(),
                task.targetExpertId(),
                task.error());
    }
}
