package com.databuff.apm.web.ai.platform.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpertTaskTextGuardTest {

    @Test
    void commitsOncePerRoundAndTask() {
        ExpertTaskTextGuard guard = new ExpertTaskTextGuard();

        assertThat(guard.tryCommitExpertTaskText("s-1", 1, "task-a")).isTrue();
        assertThat(guard.tryCommitExpertTaskText("s-1", 1, "task-a")).isFalse();
        assertThat(guard.tryCommitExpertTaskText("s-1", 1, "task-b")).isTrue();
        assertThat(guard.tryCommitExpertTaskText("s-1", 2, "task-a")).isTrue();
    }

    @Test
    void clearSessionAllowsRecommit() {
        ExpertTaskTextGuard guard = new ExpertTaskTextGuard();
        assertThat(guard.tryCommitExpertTaskText("s-2", 1, "task-a")).isTrue();

        guard.clearSession("s-2");

        assertThat(guard.tryCommitExpertTaskText("s-2", 1, "task-a")).isTrue();
    }
}
