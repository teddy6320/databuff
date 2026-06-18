package com.databuff.apm.web.ai.platform.runtime;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Active chat scopes keyed by sessionId (single centralized store, no ThreadLocal). */
public final class ExpertChatScopeRegistry {

    private static final ConcurrentMap<String, ExpertChatContext.State> ACTIVE = new ConcurrentHashMap<>();

    private ExpertChatScopeRegistry() {
    }

    public static void register(ExpertChatContext.State state) {
        if (state == null || state.sessionId() == null || state.sessionId().isBlank()) {
            return;
        }
        ACTIVE.put(state.sessionId(), state);
    }

    public static void unregister(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            ACTIVE.remove(sessionId);
        }
    }

    public static Optional<ExpertChatContext.State> find(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ACTIVE.get(sessionId.trim()));
    }

    public static Optional<ExpertChatContext.State> soleActiveState() {
        if (ACTIVE.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(ACTIVE.values().iterator().next());
    }

    public static Optional<String> soleSessionId() {
        return soleActiveState().map(ExpertChatContext.State::sessionId);
    }

    public static Optional<String> resolveSessionId(String hintSessionId) {
        if (validSessionId(hintSessionId)) {
            return Optional.of(hintSessionId.trim());
        }
        return soleSessionId();
    }

    public static boolean validSessionId(String sessionId) {
        return sessionId != null
                && !sessionId.isBlank()
                && !"anonymous".equalsIgnoreCase(sessionId.trim());
    }

    /** Clears all active scopes; for unit tests only. */
    public static void clearForTests() {
        ACTIVE.clear();
    }
}
