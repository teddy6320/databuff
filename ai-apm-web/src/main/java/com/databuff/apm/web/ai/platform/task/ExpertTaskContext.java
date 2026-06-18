package com.databuff.apm.web.ai.platform.task;

import com.databuff.apm.web.ai.platform.runtime.ExpertChatScopeRegistry;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public final class ExpertTaskContext {

    private static final ConcurrentMap<String, ConcurrentLinkedDeque<State>> SCOPES = new ConcurrentHashMap<>();

    private ExpertTaskContext() {
    }

    public static <T> T run(
            String sessionId,
            String sourceExpertId,
            Consumer<ExpertTaskEvent> eventSink,
            java.util.function.Supplier<T> action) {
        ConcurrentLinkedDeque<State> stack = SCOPES.computeIfAbsent(sessionId, key -> new ConcurrentLinkedDeque<>());
        stack.push(new State(sessionId, sourceExpertId, eventSink));
        try {
            return action.get();
        } finally {
            stack.poll();
            if (stack.isEmpty()) {
                SCOPES.remove(sessionId, stack);
            }
        }
    }

    public static void runVoid(
            String sessionId,
            String sourceExpertId,
            Consumer<ExpertTaskEvent> eventSink,
            Runnable action) {
        run(sessionId, sourceExpertId, eventSink, () -> {
            action.run();
            return null;
        });
    }

    public static void enterScope(
            String sessionId,
            String sourceExpertId,
            Consumer<ExpertTaskEvent> eventSink) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        ConcurrentLinkedDeque<State> stack = SCOPES.computeIfAbsent(sessionId, key -> new ConcurrentLinkedDeque<>());
        stack.push(new State(sessionId, sourceExpertId, eventSink));
    }

    public static void leaveScope(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        ConcurrentLinkedDeque<State> stack = SCOPES.get(sessionId);
        if (stack == null) {
            return;
        }
        stack.poll();
        if (stack.isEmpty()) {
            SCOPES.remove(sessionId, stack);
        }
    }

    public static Optional<String> sessionId() {
        return currentState().map(State::sessionId);
    }

    public static Optional<String> sourceExpertId() {
        return currentState().map(State::sourceExpertId);
    }

    public static void emit(String sessionId, ExpertTaskEvent event) {
        if (sessionId == null || sessionId.isBlank() || event == null) {
            return;
        }
        ConcurrentLinkedDeque<State> stack = SCOPES.get(sessionId);
        if (stack == null) {
            return;
        }
        State state = stack.peek();
        if (state != null && state.eventSink != null) {
            state.eventSink.accept(event);
        }
    }

    private static Optional<State> currentState() {
        Optional<State> fromActiveChat = ExpertChatScopeRegistry.soleSessionId()
                .flatMap(ExpertTaskContext::peekStack);
        if (fromActiveChat.isPresent()) {
            return fromActiveChat;
        }
        if (SCOPES.size() != 1) {
            return Optional.empty();
        }
        return peekStack(SCOPES.keySet().iterator().next());
    }

    private static Optional<State> peekStack(String sessionId) {
        ConcurrentLinkedDeque<State> stack = SCOPES.get(sessionId);
        if (stack == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(stack.peek());
    }

    /** Clears all task scopes; for unit tests only. */
    public static void clearForTests() {
        SCOPES.clear();
    }

    private record State(
            String sessionId,
            String sourceExpertId,
            Consumer<ExpertTaskEvent> eventSink) {
    }
}
