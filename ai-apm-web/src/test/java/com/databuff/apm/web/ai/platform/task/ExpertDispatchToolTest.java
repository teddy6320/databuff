package com.databuff.apm.web.ai.platform.task;

import com.databuff.apm.web.ai.TestAiSupport;
import com.databuff.apm.web.ai.UpdateLlmProviderRequest;
import com.databuff.apm.web.ai.platform.runtime.ExpertChatContext;
import com.databuff.apm.web.ai.platform.runtime.ExpertChatScopeRegistry;
import com.databuff.apm.web.ai.tool.ApmToolkit;
import io.agentscope.core.agent.RuntimeContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.time.Duration;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ExpertDispatchToolTest {

    @TempDir
    Path tempDir;

    private ExpertDispatchTool dispatchTool;
    private ExpertTaskService taskService;

    @BeforeEach
    void setUp() {
        ExpertChatScopeRegistry.clearForTests();
        ExpertTaskContext.clearForTests();
        TestAiSupport.AiFixture aiFixture = TestAiSupport.aiFixture();
        aiFixture.agentRuntimeConfig().setCustomSkillsDir(tempDir.toString());
        aiFixture.store().updateProvider("openai", new UpdateLlmProviderRequest(
                null, "sk-test", null, true));
        TestAiSupport.PlatformRuntimeFixture fixture =
                aiFixture.buildPlatformRuntime(Mockito.mock(ApmToolkit.class));
        taskService = fixture.expertTaskService();
        dispatchTool = fixture.expertDispatchTool();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (taskService != null) {
            taskService.shutdownForTests();
        }
        ExpertChatScopeRegistry.clearForTests();
        ExpertTaskContext.clearForTests();
    }

    @Test
    void dispatchUsesChatScopeWhenTaskContextMissing() {
        String sessionId = "s-scope";
        ExpertChatScopeRegistry.register(new ExpertChatContext.State(
                sessionId, "admin", "brain", null, false, null));
        try {
            dispatchTool.dispatchExpertTask("data", "count spans", "{}", null);
            ExpertTask created = taskService.listBySession(sessionId).stream()
                    .findFirst()
                    .orElseThrow();
            assertThat(created.sessionId()).isEqualTo(sessionId);
            assertThat(taskService.listBySession("anonymous")).isEmpty();
        } finally {
            ExpertChatScopeRegistry.unregister(sessionId);
        }
    }

    @Test
    void dispatchCreatesTaskInContext() throws Exception {
        List<ExpertTaskEvent> events = new ArrayList<>();
        String result = ExpertTaskContext.run("s-dispatch", "brain", events::add, () ->
                dispatchTool.dispatchExpertTask("data", "count spans", "{}", null));
        ExpertTask finished = taskService.listBySession("s-dispatch").stream()
                .findFirst()
                .orElseThrow();
        taskService.waitFor(finished.taskId(), Duration.ofSeconds(5));

        assertThat(result).contains("taskId=" + finished.taskId());
        assertThat(result).contains("请静静等待");
        Assertions.assertThat(events).isNotEmpty();
        assertThat(events.get(0).type()).isEqualTo("subtask.created");
        assertThat(finished.status()).isIn(
                ExpertTaskStatus.CREATED,
                ExpertTaskStatus.RUNNING,
                ExpertTaskStatus.SUCCEEDED,
                ExpertTaskStatus.FAILED,
                ExpertTaskStatus.TIMEOUT,
                ExpertTaskStatus.CANCELLED);
    }

    @Test
    void dispatchUsesRuntimeContextWhenMultipleChatScopesActive() {
        ExpertChatScopeRegistry.register(new ExpertChatContext.State(
                "s-other", "admin", "brain", null, false, null));
        ExpertChatScopeRegistry.register(new ExpertChatContext.State(
                "s-target", "admin", "brain", null, false, null));
        RuntimeContext runtimeContext = RuntimeContext.builder().sessionId("s-target").build();
        try {
            dispatchTool.dispatchExpertTask("data", "count spans", "{}", runtimeContext);
            assertThat(taskService.listBySession("s-target")).hasSize(1);
            assertThat(taskService.listBySession("s-other")).isEmpty();
            assertThat(taskService.listBySession("anonymous")).isEmpty();
        } finally {
            ExpertChatScopeRegistry.unregister("s-other");
            ExpertChatScopeRegistry.unregister("s-target");
        }
    }

    @Test
    void dispatchFailsWhenSessionUnavailable() {
        assertThatThrownBy(() -> dispatchTool.dispatchExpertTask("data", "count spans", "{}", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sessionId unavailable");
    }
}
