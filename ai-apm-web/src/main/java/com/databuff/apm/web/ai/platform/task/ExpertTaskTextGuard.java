package com.databuff.apm.web.ai.platform.task;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ExpertTaskTextGuard {

    private final ConcurrentMap<String, Boolean> committed = new ConcurrentHashMap<>();

    public boolean tryCommitExpertTaskText(String sessionId, int roundIndex, String taskId) {
        if (sessionId == null || sessionId.isBlank() || taskId == null || taskId.isBlank()) {
            return false;
        }
        String key = key(sessionId, roundIndex, taskId);
        return committed.putIfAbsent(key, Boolean.TRUE) == null;
    }

    public void clearSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        String prefix = sessionId.trim() + ":";
        committed.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private static String key(String sessionId, int roundIndex, String taskId) {
        return sessionId.trim() + ":" + roundIndex + ":" + taskId.trim();
    }
}
