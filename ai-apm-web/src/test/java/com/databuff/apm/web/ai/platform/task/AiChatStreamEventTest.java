package com.databuff.apm.web.ai.platform.task;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiChatStreamEventTest {

    @Test
    void expertTaskEventTypesMatchStreamContract() {
        ExpertTask task = new ExpertTask(
                "t-1",
                null,
                "s-1",
                "brain",
                "data",
                ExpertTaskStatus.RUNNING,
                "input",
                null,
                null,
                java.util.Map.of(),
                java.time.Instant.now(),
                java.time.Instant.now(),
                null);
        List<String> types = List.of(
                ExpertTaskEvent.created(task).type(),
                ExpertTaskEvent.running(task).type(),
                ExpertTaskEvent.completed(task.withStatus(
                        ExpertTaskStatus.SUCCEEDED, "ok", null,
                        java.time.Instant.now(), java.time.Instant.now())).type(),
                ExpertTaskEvent.failed(task.withStatus(
                        ExpertTaskStatus.FAILED, null, "err",
                        java.time.Instant.now(), java.time.Instant.now())).type());
        assertThat(types).containsExactly(
                "subtask.created",
                "subtask.running",
                "subtask.completed",
                "subtask.failed");
    }
}
