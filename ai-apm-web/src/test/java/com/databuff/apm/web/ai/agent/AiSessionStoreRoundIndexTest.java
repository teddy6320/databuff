package com.databuff.apm.web.ai.agent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiSessionStoreRoundIndexTest {

    @Test
    void userMessageStartsNewRound() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1", "alice");

        store.appendUserMessage(sessionId, "你好", "brain", "alice", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "brain");
        store.appendOrUpdateAssistantText(
                sessionId, assistantId, "brain", "你好，我是助手", AiMessageStatus.COMPLETED, Map.of());
        store.completeRound(sessionId, "brain");

        assertThat(store.messages(sessionId))
                .hasSize(2)
                .satisfiesExactly(
                        user -> {
                            assertThat(user.messageType()).isEqualTo("USER");
                            assertThat(user.roundIndex()).isEqualTo(1);
                            assertThat(user.messageIndex()).isEqualTo(1);
                        },
                        assistant -> {
                            assertThat(assistant.messageType()).isEqualTo("TEXT");
                            assertThat(assistant.roundIndex()).isEqualTo(1);
                            assertThat(assistant.messageIndex()).isEqualTo(2);
                        });

        store.appendUserMessage(sessionId, "第二问", "brain", "alice", Map.of());
        assertThat(store.messages(sessionId).get(2))
                .satisfies(message -> {
                    assertThat(message.messageType()).isEqualTo("USER");
                    assertThat(message.roundIndex()).isEqualTo(2);
                    assertThat(message.messageIndex()).isEqualTo(3);
                });
    }

    @Test
    void finalTextUsesLargestMessageIndexAfterTraceEvents() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "data", "rk", "web-1");
        store.appendUserMessage(sessionId, "查服务列表", "data", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "data");

        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.REASONING,
                "先查询实体列表",
                AiMessageStatus.COMPLETED,
                Map.of());
        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.TOOL_CALL,
                "工具调用开始：queryServicesAll",
                AiMessageStatus.COMPLETED,
                Map.of("toolName", "queryServicesAll", "toolCallId", "call-1"));
        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.TOOL_RESULT,
                "工具调用结果：queryServicesAll",
                AiMessageStatus.COMPLETED,
                Map.of("toolName", "queryServicesAll", "toolCallId", "call-1"));
        store.appendOrUpdateAssistantText(
                sessionId,
                assistantId,
                "data",
                "最终回答",
                AiMessageStatus.COMPLETED,
                Map.of());
        store.completeRound(sessionId, "data");

        assertThat(store.messages(sessionId))
                .extracting(AiSessionStore.ChatMessage::messageType)
                .containsExactly("USER", "REASONING", "TOOL_CALL", "TOOL_RESULT", "TEXT");
        assertThat(store.messages(sessionId))
                .extracting(AiSessionStore.ChatMessage::messageIndex)
                .containsExactly(1, 2, 3, 4, 5);

        AiSessionStore.ChatMessage finalText = store.messages(sessionId).get(4);
        assertThat(finalText.messageType()).isEqualTo("TEXT");
        assertThat(finalText.messageIndex()).isEqualTo(5);
    }

    @Test
    void streamingReasoningAccumulatesChunks() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1");
        store.appendUserMessage(sessionId, "hi", "brain", "admin", Map.of());

        store.appendTraceMessage(
                sessionId, "brain", "admin", AiMessageType.REASONING, "思考", AiMessageStatus.STREAMING, Map.of());
        store.appendTraceMessage(
                sessionId, "brain", "admin", AiMessageType.REASONING, "中", AiMessageStatus.STREAMING, Map.of());

        AiSessionStore.ChatMessage reasoning = store.activeRoundMessages(sessionId).stream()
                .filter(message -> AiMessageType.REASONING.name().equals(message.messageType()))
                .findFirst()
                .orElseThrow();
        assertThat(reasoning.content()).isEqualTo("思考中");
        assertThat(reasoning.messageStatus()).isEqualTo("STREAMING");

        store.finalizeRoundStreaming(sessionId, "brain");
        assertThat(store.activeRoundMessages(sessionId).stream()
                .filter(message -> AiMessageType.REASONING.name().equals(message.messageType()))
                .findFirst()
                .orElseThrow()
                .messageStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void hookTraceUsesSameRoundAsUser() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "data", "rk", "web-1");
        store.appendUserMessage(sessionId, "查错误率", "data", "admin", Map.of());

        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.TOOL_CALL,
                "调用工具 serviceErrorRate",
                AiMessageStatus.COMPLETED,
                Map.of("toolName", "serviceErrorRate"));

        AiSessionStore.ChatMessage toolCall = store.activeRoundMessages(sessionId).get(1);
        assertThat(toolCall.messageType()).isEqualTo("TOOL_CALL");
        assertThat(toolCall.roundIndex()).isEqualTo(1);
        assertThat(toolCall.messageIndex()).isEqualTo(2);
        assertThat(toolCall.messageStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void assistantTextDoesNotOverwriteReasoningWithSameMessageId() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1");
        store.appendUserMessage(sessionId, "hello", "brain", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "brain");
        store.appendMessage(sessionId, new AiSessionStore.ChatMessage(
                assistantId,
                "assistant",
                "推理内容",
                "brain",
                AiMessageType.REASONING.name(),
                AiMessageStatus.STREAMING.name(),
                Map.of(),
                1,
                2,
                java.time.Instant.now(),
                java.time.Instant.now()));

        store.appendOrUpdateAssistantText(
                sessionId, assistantId, "brain", "最终回答", AiMessageStatus.COMPLETED, Map.of());

        assertThat(store.activeRoundMessages(sessionId))
                .filteredOn(message -> AiMessageType.REASONING.name().equals(message.messageType()))
                .hasSize(1)
                .first()
                .satisfies(message -> assertThat(message.content()).isEqualTo("推理内容"));
        assertThat(store.activeRoundMessages(sessionId))
                .filteredOn(message -> AiMessageType.TEXT.name().equals(message.messageType()))
                .hasSize(1)
                .first()
                .satisfies(message -> {
                    assertThat(message.content()).isEqualTo("最终回答");
                    assertThat(message.messageId()).isNotEqualTo(assistantId);
                });
    }

    @Test
    void endReasoningSegmentAllowsNewReasoningAfterToolCall() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "data", "rk", "web-1");
        store.appendUserMessage(sessionId, "查指标", "data", "admin", Map.of());

        store.appendTraceMessage(
                sessionId, "data", "admin", AiMessageType.REASONING, "先查实体", AiMessageStatus.STREAMING, Map.of());
        store.endReasoningSegment(sessionId, "data");
        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.TOOL_CALL,
                "工具调用开始：queryServicesAll",
                AiMessageStatus.COMPLETED,
                Map.of("toolName", "queryServicesAll", "toolCallId", "call-1"));
        store.appendTraceMessage(
                sessionId, "data", "admin", AiMessageType.REASONING, "继续分析", AiMessageStatus.STREAMING, Map.of());

        assertThat(store.activeRoundMessages(sessionId))
                .filteredOn(message -> AiMessageType.REASONING.name().equals(message.messageType()))
                .hasSize(2)
                .extracting(AiSessionStore.ChatMessage::content)
                .containsExactly("先查实体", "继续分析");
    }

    @Test
    void reservedAssistantTextIsNotCreatedBeforeFirstAnswerChunk() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "data", "rk", "web-1");
        store.appendUserMessage(sessionId, "查服务列表", "data", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "data");

        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.REASONING,
                "先查询实体列表",
                AiMessageStatus.COMPLETED,
                Map.of());
        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.TOOL_CALL,
                "工具调用开始：queryServicesAll",
                AiMessageStatus.COMPLETED,
                Map.of("toolName", "queryServicesAll", "toolCallId", "call-1"));
        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.TOOL_RESULT,
                "工具调用结果：queryServicesAll",
                AiMessageStatus.COMPLETED,
                Map.of("toolName", "queryServicesAll", "toolCallId", "call-1", "toolResult", "{\"ok\":true}"));

        assertThat(store.activeRoundMessages(sessionId))
                .extracting(AiSessionStore.ChatMessage::messageType)
                .containsExactly("USER", "REASONING", "TOOL_CALL", "TOOL_RESULT");
        assertThat(store.activeRoundMessages(sessionId))
                .extracting(AiSessionStore.ChatMessage::messageIndex)
                .containsExactly(1, 2, 3, 4);

        store.appendOrUpdateAssistantText(
                sessionId, assistantId, "data", "最终回答", AiMessageStatus.COMPLETED, Map.of());
        store.completeRound(sessionId, "data");

        assertThat(store.messages(sessionId))
                .extracting(AiSessionStore.ChatMessage::messageType)
                .containsExactly("USER", "REASONING", "TOOL_CALL", "TOOL_RESULT", "TEXT");
        assertThat(store.messages(sessionId))
                .extracting(AiSessionStore.ChatMessage::messageIndex)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void updateToolCallInputStoresParametersOnToolCallRow() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "data", "rk", "web-1");
        store.appendUserMessage(sessionId, "查指标", "data", "admin", Map.of());
        store.appendTraceMessage(
                sessionId,
                "data",
                "admin",
                AiMessageType.TOOL_CALL,
                "工具调用开始：queryServicesAll",
                AiMessageStatus.COMPLETED,
                Map.of("toolName", "queryServicesAll", "toolCallId", "call-1"));

        store.updateToolCallInput(sessionId, "call-1", "{\"service\":\"demo\"}");

        AiSessionStore.ChatMessage toolCall = store.activeRoundMessages(sessionId).get(1);
        assertThat(toolCall.messageType()).isEqualTo("TOOL_CALL");
        assertThat(toolCall.metadata()).containsEntry("toolInput", "{\"service\":\"demo\"}");
    }

    @Test
    void roundFlushListenerReceivesOrderedSnapshot() {
        AiSessionStore store = new AiSessionStore();
        List<String> flushed = new ArrayList<>();
        store.setRoundFlushListener((sessionId, messages) -> messages.forEach(message ->
                flushed.add(message.messageType() + ":" + message.messageIndex())));

        String sessionId = store.ensureSession(null, "brain", "rk", "web-1");
        store.appendUserMessage(sessionId, "hello", "brain", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "brain");
        store.appendTraceMessage(
                sessionId, "brain", "admin", AiMessageType.TOOL_CALL, "call", AiMessageStatus.COMPLETED, Map.of());
        store.appendOrUpdateAssistantText(
                sessionId, assistantId, "brain", "done", AiMessageStatus.COMPLETED, Map.of());
        store.completeRound(sessionId, "brain");

        assertThat(flushed).containsExactly("USER:1", "TOOL_CALL:2", "TEXT:3");
    }
}
