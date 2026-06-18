package com.databuff.apm.web.ai.agent;

import com.databuff.apm.web.ai.TestAiSupport;
import com.databuff.apm.web.ai.tool.ApmToolkit;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentControllerPollTest {

    @Test
    void submitAndPollUntilComplete() throws Exception {
        ApmToolkit toolkit = mock(ApmToolkit.class);
        when(toolkit.countRecentSpans(anyLong())).thenReturn(7);
        AgentBrainService brain = TestAiSupport.aiFixture().agentBrain(toolkit, new AiSessionStore());
        AgentController controller = TestAiSupport.aiFixture().agentController(brain);
        HttpServletRequest request = mock(HttpServletRequest.class);

        AgentBrainService.ChatSubmitResponse submitted = controller.submit(
                request,
                new AgentBrainService.ChatRequest(null, "最近 trace 有多少"));
        assertThat(submitted.sessionId()).isNotBlank();
        assertThat(submitted.assistantMessageId()).isNotBlank();
        assertThat(submitted.status()).isEqualTo("PROCESSING");

        AiSessionStore.MessagePollResponse poll = waitUntilDone(controller, submitted.sessionId());
        assertThat(poll.running()).isFalse();
        assertThat(poll.messages()).anyMatch(message ->
                "assistant".equals(message.role()) && message.content().contains("7"));
    }

    @Test
    void abortStopsRunningSession() throws Exception {
        ApmToolkit toolkit = mock(ApmToolkit.class);
        when(toolkit.countRecentSpans(anyLong())).thenAnswer(invocation -> {
            Thread.sleep(500L);
            return 9;
        });
        AgentBrainService brain = TestAiSupport.aiFixture().agentBrain(toolkit, new AiSessionStore());
        AgentController controller = TestAiSupport.aiFixture().agentController(brain);
        HttpServletRequest request = mock(HttpServletRequest.class);

        AgentBrainService.ChatSubmitResponse submitted = controller.submit(
                request,
                new AgentBrainService.ChatRequest(null, "最近 trace 有多少"));
        AiSessionStore.MessagePollResponse running = controller.messages(submitted.sessionId(), null);
        assertThat(running.running()).isTrue();

        AgentBrainService.ChatAbortResponse aborted = controller.abort(submitted.sessionId());
        assertThat(aborted.aborted()).isTrue();

        AiSessionStore.MessagePollResponse poll = waitUntilDone(controller, submitted.sessionId());
        assertThat(poll.running()).isFalse();
        assertThat(poll.messages()).anyMatch(message ->
                submitted.assistantMessageId().equals(message.messageId())
                        && AiMessageStatus.CANCELLED.name().equals(message.messageStatus()));
        assertThat(poll.messages()).noneMatch(message ->
                message.content() != null && message.content().contains("对话失败"));
    }

    @Test
    void pollAfterMessageIdReturnsIncrementalMessages() throws Exception {
        ApmToolkit toolkit = mock(ApmToolkit.class);
        when(toolkit.countRecentSpans(anyLong())).thenReturn(1);
        AgentBrainService brain = TestAiSupport.aiFixture().agentBrain(toolkit, new AiSessionStore());
        AgentController controller = TestAiSupport.aiFixture().agentController(brain);
        HttpServletRequest request = mock(HttpServletRequest.class);

        AgentBrainService.ChatSubmitResponse first = controller.submit(
                request,
                new AgentBrainService.ChatRequest(null, "trace"));
        waitUntilDone(controller, first.sessionId());

        AiSessionStore.MessagePollResponse baseline = controller.messages(first.sessionId(), null);
        String lastMessageId = baseline.messages().get(baseline.messages().size() - 1).messageId();

        controller.submit(request, new AgentBrainService.ChatRequest(first.sessionId(), "help"));
        waitUntilDone(controller, first.sessionId());

        AiSessionStore.MessagePollResponse incremental =
                controller.messages(first.sessionId(), lastMessageId);
        assertThat(incremental.messages()).isNotEmpty();
    }

    private static AiSessionStore.MessagePollResponse waitUntilDone(
            AgentController controller,
            String sessionId) throws InterruptedException {
        AiSessionStore.MessagePollResponse poll = null;
        for (int i = 0; i < 80; i++) {
            poll = controller.messages(sessionId, null);
            if (!poll.running()) {
                return poll;
            }
            Thread.sleep(50L);
        }
        return poll;
    }
}
