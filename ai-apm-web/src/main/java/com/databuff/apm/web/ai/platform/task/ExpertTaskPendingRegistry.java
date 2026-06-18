package com.databuff.apm.web.ai.platform.task;

import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExpertTaskPendingRegistry {

    private final ConcurrentHashMap<String, Set<String>> pendingBySession = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, java.util.concurrent.Future<?>> taskFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Disposable> taskDisposables = new ConcurrentHashMap<>();

    public void addPending(String sessionId, String taskId) {
        if (blank(sessionId) || blank(taskId)) {
            return;
        }
        pendingBySession.computeIfAbsent(sessionId.trim(), key -> ConcurrentHashMap.newKeySet()).add(taskId.trim());
    }

    public void removePending(String sessionId, String taskId) {
        if (sessionId == null || taskId == null) {
            return;
        }
        Set<String> pending = pendingBySession.get(sessionId.trim());
        if (pending != null) {
            pending.remove(taskId.trim());
            if (pending.isEmpty()) {
                pendingBySession.remove(sessionId.trim(), pending);
            }
        }
    }

    public boolean hasPending(String sessionId) {
        if (blank(sessionId)) {
            return false;
        }
        Set<String> pending = pendingBySession.get(sessionId.trim());
        return pending != null && !pending.isEmpty();
    }

    public boolean isPending(String sessionId, String taskId) {
        if (blank(sessionId) || blank(taskId)) {
            return false;
        }
        Set<String> pending = pendingBySession.get(sessionId.trim());
        return pending != null && pending.contains(taskId.trim());
    }

    public Set<String> pendingTaskIds(String sessionId) {
        if (blank(sessionId)) {
            return Set.of();
        }
        Set<String> pending = pendingBySession.get(sessionId.trim());
        return pending == null ? Set.of() : Set.copyOf(pending);
    }

    public void clearSession(String sessionId) {
        if (blank(sessionId)) {
            return;
        }
        pendingBySession.remove(sessionId.trim());
        String prefix = sessionId.trim() + ":";
        taskFutures.keySet().removeIf(key -> key.startsWith(prefix));
        taskDisposables.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public void registerTaskFuture(String sessionId, String taskId, java.util.concurrent.Future<?> future) {
        if (blank(sessionId) || blank(taskId) || future == null) {
            return;
        }
        taskFutures.put(taskKey(sessionId, taskId), future);
    }

    public void registerTaskDisposable(String sessionId, String taskId, Disposable disposable) {
        if (blank(sessionId) || blank(taskId) || disposable == null) {
            return;
        }
        taskDisposables.put(taskKey(sessionId, taskId), disposable);
    }

    public void removeTaskDisposable(String sessionId, String taskId) {
        if (blank(sessionId) || blank(taskId)) {
            return;
        }
        taskDisposables.remove(taskKey(sessionId, taskId));
    }

    public void cancelSessionTasks(String sessionId) {
        if (blank(sessionId)) {
            return;
        }
        String prefix = sessionId.trim() + ":";
        taskFutures.forEach((key, future) -> {
            if (key.startsWith(prefix) && future != null && !future.isDone()) {
                future.cancel(true);
            }
        });
        taskDisposables.forEach((key, disposable) -> {
            if (key.startsWith(prefix) && disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        });
        taskFutures.keySet().removeIf(key -> key.startsWith(prefix));
        taskDisposables.keySet().removeIf(key -> key.startsWith(prefix));
        clearSession(sessionId);
    }

    private static String taskKey(String sessionId, String taskId) {
        return sessionId.trim() + ":" + taskId.trim();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
