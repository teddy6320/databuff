package com.databuff.apm.web.ai.agent;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiSessionStoreTest {

    @Test
    void reusesExistingSession() {
        AiSessionStore store = new AiSessionStore();
        String first = store.ensureSession(null);
        String again = store.ensureSession(first);
        assertThat(again).isEqualTo(first);
    }

    @Test
    void ignoresAppendForMissingSession() {
        AiSessionStore store = new AiSessionStore();
        store.append("missing", "user", "hello");
        assertThat(store.messages("missing")).isEmpty();
    }

    @Test
    void restoresHydratedSession() {
        AiSessionStore store = new AiSessionStore();
        store.restore("s1", Instant.now(), List.of(new AiSessionStore.ChatMessage("user", "hi", Instant.now())));
        assertThat(store.messages("s1")).hasSize(1);
    }

    @Test
    void abortSessionMarksAssistantMessageCancelled() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "brain", "rk", "web-1");
        store.appendUserMessage(sessionId, "hello", "brain", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "brain");
        store.setRunning(sessionId, true);
        store.appendTraceMessage(
                sessionId,
                "brain",
                "admin",
                AiMessageType.REASONING,
                "thinking",
                AiMessageStatus.STREAMING,
                Map.of());

        boolean aborted = store.abortSession(sessionId);

        assertThat(aborted).isTrue();
        assertThat(store.isRunning(sessionId)).isFalse();
        assertThat(store.messages(sessionId)).anyMatch(message ->
                assistantId.equals(message.messageId())
                        && AiMessageStatus.CANCELLED.name().equals(message.messageStatus())
                        && "已中止".equals(message.content()));
    }

    @Test
    void abortSessionPreservesPartialAssistantReply() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "data", "rk", "web-1");
        store.appendUserMessage(sessionId, "metrics", "data", "admin", Map.of());
        String assistantId = store.reserveAssistantMessageId(sessionId, "data");
        store.setRunning(sessionId, true);
        store.appendOrUpdateAssistantText(
                sessionId,
                assistantId,
                "data",
                "部分回答",
                AiMessageStatus.STREAMING,
                Map.of());

        boolean aborted = store.abortSession(sessionId);

        assertThat(aborted).isTrue();
        assertThat(store.messages(sessionId)).anyMatch(message ->
                assistantId.equals(message.messageId())
                        && AiMessageStatus.CANCELLED.name().equals(message.messageStatus())
                        && "部分回答".equals(message.content()));
    }

    @Test
    void listsSessionsByRecency() throws InterruptedException {
        AiSessionStore store = new AiSessionStore();
        String older = store.ensureSession(null);
        store.append(older, "user", "a");
        Thread.sleep(5);
        String newer = store.ensureSession(null);
        store.append(newer, "user", "b");
        assertThat(store.listSessions()).extracting(AiSessionStore.SessionSummary::sessionId)
                .containsExactly(newer, older);
    }
}
