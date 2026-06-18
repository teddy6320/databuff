package com.databuff.apm.web.ai.platform.task;

import io.agentscope.core.agent.RuntimeContext;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ExpertSessionResolverTest {

    @Test
    void normalizeChatSessionIdStripsTaskSuffix() {
        assertThat(ExpertSessionResolver.normalizeChatSessionId("abc#task:task-1")).isEqualTo("abc");
    }

    @Test
    void normalizeChatSessionIdRejectsAnonymous() {
        assertThat(ExpertSessionResolver.normalizeChatSessionId("anonymous")).isNull();
    }

    @Test
    void sessionIdFromRuntimeContextPrefersRuntimeSessionId() {
        RuntimeContext context = RuntimeContext.builder()
                .sessionId("session-from-runtime")
                .build();
        assertThat(ExpertSessionResolver.sessionIdFromRuntimeContext(context))
                .contains("session-from-runtime");
    }

    @Test
    void resolveSessionIdUsesRuntimeContextWhenScopesAreAmbiguous() {
        RuntimeContext context = RuntimeContext.builder()
                .sessionId("session-runtime")
                .build();
        Optional<String> resolved = ExpertSessionResolver.resolveSessionId(null, context);
        assertThat(resolved).contains("session-runtime");
    }
}
