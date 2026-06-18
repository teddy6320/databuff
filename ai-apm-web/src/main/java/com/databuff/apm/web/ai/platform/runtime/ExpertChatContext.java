package com.databuff.apm.web.ai.platform.runtime;

import java.util.Optional;
import java.util.function.Consumer;

/** Per-request chat scope for AgentScope hooks and streaming callbacks. */
public final class ExpertChatContext {

    private ExpertChatContext() {
    }

    public static Optional<State> current() {
        return ExpertChatScopeRegistry.soleActiveState();
    }

    public static Optional<State> resolve(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            Optional<State> found = ExpertChatScopeRegistry.find(sessionId);
            if (found.isPresent()) {
                return found;
            }
        }
        return ExpertChatScopeRegistry.soleActiveState();
    }

    public static <T> T run(State state, java.util.function.Supplier<T> action) {
        ExpertChatScopeRegistry.register(state);
        try {
            return action.get();
        } finally {
            ExpertChatScopeRegistry.unregister(state.sessionId());
        }
    }

    public static void runVoid(State state, Runnable action) {
        run(state, () -> {
            action.run();
            return null;
        });
    }

    public record State(
            String sessionId,
            String userName,
            String expertId,
            String assistantMessageId,
            boolean exposeToolEvents,
            Consumer<ExpertRuntimeEvent> streamSink,
            String taskId) {

        public State(
                String sessionId,
                String userName,
                String expertId,
                String assistantMessageId,
                boolean exposeToolEvents,
                Consumer<ExpertRuntimeEvent> streamSink) {
            this(sessionId, userName, expertId, assistantMessageId, exposeToolEvents, streamSink, null);
        }

        public void emit(ExpertRuntimeEvent event) {
            if (streamSink != null && event != null) {
                streamSink.accept(event);
            }
        }
    }
}
