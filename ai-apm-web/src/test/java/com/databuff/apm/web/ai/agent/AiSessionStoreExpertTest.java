package com.databuff.apm.web.ai.agent;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiSessionStoreExpertTest {

    @Test
    void storesExpertIdOnSessionAndMessages() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null, "data", "session:key", "web-1");
        store.appendUserMessage(sessionId, "最近错误率", "data", "admin", Map.of("service", "demo-order"));
        store.appendAssistantMessage(sessionId, "ok", "data", Map.of());

        assertThat(store.listSessions()).singleElement()
                .satisfies(summary -> {
                    assertThat(summary.expertId()).isEqualTo("data");
                    assertThat(summary.title()).contains("最近错误率");
                });
        assertThat(store.messages(sessionId))
                .hasSize(2)
                .allSatisfy(message -> assertThat(message.expertId()).isEqualTo("data"));
    }

    @Test
    void backwardCompatibleAppendWithoutExpert() {
        AiSessionStore store = new AiSessionStore();
        String sessionId = store.ensureSession(null);
        store.append(sessionId, "user", "hello");
        assertThat(store.messages(sessionId)).singleElement()
                .satisfies(message -> {
                    assertThat(message.expertId()).isNull();
                    assertThat(message.messageType()).isEqualTo("USER");
                });
    }
}
