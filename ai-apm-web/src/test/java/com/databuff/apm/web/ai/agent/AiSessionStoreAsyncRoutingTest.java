package com.databuff.apm.web.ai.agent;

import com.databuff.apm.web.ai.platform.task.ExpertMessageConstants;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiSessionStoreAsyncRoutingTest {

    @Test
    void appendExpertDeliverableTextMarksMetadata() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1", "admin");
        store.appendUserMessage(sessionId, "查服务", "brain", "admin", Map.of());

        store.appendExpertDeliverableText(
                sessionId,
                "data",
                1,
                "task-1",
                "最近1小时共 3 个服务",
                Map.of());

        assertThat(store.messages(sessionId))
                .anySatisfy(message -> {
                    assertThat(message.messageType()).isEqualTo("TEXT");
                    assertThat(message.expertId()).isEqualTo("data");
                    assertThat(message.metadata())
                            .containsEntry(ExpertMessageConstants.META_IS_EXPERT_DELIVERABLE, true)
                            .containsEntry(ExpertMessageConstants.META_IS_ROUND_FINAL, false)
                            .containsEntry(ExpertMessageConstants.META_TASK_ID, "task-1")
                            .containsEntry(ExpertMessageConstants.META_ROUND_INDEX, 1);
                    assertThat(message.content()).contains("3 个服务");
                });
    }

    @Test
    void appendBrainIntermediateTextCreatesReasoning() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1", "admin");
        store.appendUserMessage(sessionId, "查服务", "brain", "admin", Map.of());

        store.appendBrainIntermediateText(sessionId, "brain", "已派发，请等待", Map.of());

        assertThat(store.messages(sessionId))
                .anySatisfy(message -> {
                    assertThat(message.messageType()).isEqualTo("REASONING");
                    assertThat(message.expertId()).isEqualTo("brain");
                    assertThat(message.content()).contains("请等待");
                });
        assertThat(store.messages(sessionId))
                .noneMatch(message -> Boolean.TRUE.equals(message.metadata().get(ExpertMessageConstants.META_IS_ROUND_FINAL)));
    }

    @Test
    void appendBrainIntermediateTextSkipsDuplicateReasoningContent() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1", "admin");
        store.appendUserMessage(sessionId, "查服务", "brain", "admin", Map.of());
        store.appendTraceMessage(
                sessionId,
                "brain",
                "admin",
                AiMessageType.REASONING,
                "已派发，请等待",
                AiMessageStatus.COMPLETED,
                Map.of());

        store.appendBrainIntermediateText(sessionId, "brain", "已派发，请等待", Map.of("modelName", "MiniMax-M3"));

        assertThat(store.messages(sessionId))
                .filteredOn(message -> "REASONING".equals(message.messageType()))
                .hasSize(1);
    }

    @Test
    void demoteBrainNonFinalTextToReasoningWhileTasksOutstanding() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1", "admin");
        store.appendUserMessage(sessionId, "查服务", "brain", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "brain");
        store.appendOrUpdateAssistantText(
                sessionId,
                assistantId,
                "brain",
                "已派发，请等待专家返回",
                AiMessageStatus.COMPLETED,
                Map.of());

        store.demoteBrainNonFinalTextToReasoning(sessionId, "brain", 1);

        assertThat(store.messages(sessionId))
                .anySatisfy(message -> {
                    assertThat(message.messageId()).isEqualTo(assistantId);
                    assertThat(message.messageType()).isEqualTo("REASONING");
                    assertThat(message.content()).contains("请等待");
                })
                .noneMatch(message -> "TEXT".equals(message.messageType())
                        && "brain".equals(message.expertId())
                        && Boolean.TRUE.equals(message.metadata().get(ExpertMessageConstants.META_IS_ROUND_FINAL)));
    }

    @Test
    void finalizeBrainRoundTextCompletesRound() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1", "admin");
        store.appendUserMessage(sessionId, "查服务", "brain", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "brain");

        store.finalizeBrainRoundText(sessionId, assistantId, "brain", "整合后的终稿", Map.of());

        assertThat(store.messages(sessionId))
                .anySatisfy(message -> {
                    assertThat(message.messageType()).isEqualTo("TEXT");
                    assertThat(message.expertId()).isEqualTo("brain");
                    assertThat(message.metadata())
                            .containsEntry(ExpertMessageConstants.META_IS_ROUND_FINAL, true)
                            .containsEntry(ExpertMessageConstants.META_IS_EXPERT_DELIVERABLE, false);
                    assertThat(message.content()).isEqualTo("整合后的终稿");
                });
        assertThat(store.isRunning(sessionId)).isFalse();

        store.appendUserMessage(sessionId, "第二问", "brain", "admin", Map.of());
        assertThat(store.messages(sessionId).get(store.messages(sessionId).size() - 1).roundIndex()).isEqualTo(2);
    }
}
