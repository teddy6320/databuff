package com.databuff.apm.web.persistence;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.ai.agent.AiMessageType;
import com.databuff.apm.web.ai.agent.AiSessionStore;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiSessionPersistence {

    private static final Logger log = LoggerFactory.getLogger(AiSessionPersistence.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final int HYDRATE_LIMIT = 50;

    private final ApmReadRepository readRepository;
    private final AiSessionStore sessionStore;
    private final AiMessagePersistenceQueue persistenceQueue;
    private final String configDatabase;
    private volatile boolean persistenceEnabled;

    public AiSessionPersistence(
            ApmReadRepository readRepository,
            AiSessionStore sessionStore,
            AiMessagePersistenceQueue persistenceQueue,
            ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.sessionStore = sessionStore;
        this.persistenceQueue = persistenceQueue;
        this.configDatabase = storageProperties.configDatabase();
        this.persistenceQueue.setPersistenceReady(this::ensurePersistenceReady);
        this.sessionStore.setRoundFlushListener(this::onRoundFlushed);
        this.sessionStore.setMessageAppendListener(this::onMessageAppended);
    }

    void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.aiMessageSchemaReady()) {
            log.info("AI message store not ready; AI sessions stay in-memory only");
            return;
        }
        persistenceEnabled = true;
        try {
            List<ApmConfigRepository.AiSessionSummaryRow> sessions = repository.loadRecentAiSessions(HYDRATE_LIMIT);
            for (ApmConfigRepository.AiSessionSummaryRow session : sessions) {
                List<ApmConfigRepository.AiMessageRow> messages = repository.loadAiMessages(session.sessionId());
                String title = deriveTitle(messages);
                sessionStore.hydrateSession(
                        session.sessionId(),
                        session.agent(),
                        "USER",
                        title,
                        null,
                        null,
                        session.userName(),
                        messages.stream().map(this::toChatMessage).toList());
            }
            log.info("AI message persistence enabled ({} sessions from store)", sessions.size());
        } catch (Exception e) {
            log.warn("Failed to hydrate AI messages from store (writes still enabled): {}", e.getMessage());
        }
    }

    public AiSessionStore.MessagePollResponse pollMergedMessages(String sessionId, String afterMessageId) {
        ensureSessionHydrated(sessionId);
        return sessionStore.pollMessages(sessionId, afterMessageId);
    }

    public void onRoundFlushed(String sessionId, List<AiSessionStore.ChatMessage> messages) {
        if (!ensurePersistenceReady() || messages == null || messages.isEmpty()) {
            return;
        }
        AiSessionStore.SessionSummary summary = sessionStore.getSession(sessionId);
        List<ApmConfigRepository.AiMessageRow> rows = new ArrayList<>(messages.size());
        for (AiSessionStore.ChatMessage message : messages) {
            rows.add(toMessageRow(sessionId, message, summary));
        }
        persistenceQueue.enqueueRound(sessionId, rows);
    }

    /** @deprecated sessions are derived from messages; kept for compatibility */
    public void onSessionCreated(String sessionId) {
        onSessionCreated(sessionId, null);
    }

    /** @deprecated sessions are derived from messages; kept for compatibility */
    public void onSessionCreated(String sessionId, String userName) {
        // no-op: config_ai_session removed
    }

    public void onMessageAppended(String sessionId, AiSessionStore.ChatMessage message) {
        if (!ensurePersistenceReady() || message == null) {
            return;
        }
        AiSessionStore.SessionSummary summary = sessionStore.getSession(sessionId);
        persistenceQueue.enqueueRound(sessionId, List.of(toMessageRow(sessionId, message, summary)));
    }

    /** @deprecated legacy hook; flushes a synthetic single-message round */
    public void onMessageAppended(String sessionId, String role, String content, Instant ts) {
        if (!persistenceEnabled) {
            return;
        }
        Instant updatedAt = ts == null ? Instant.now() : ts;
        onMessageAppended(sessionId, new AiSessionStore.ChatMessage(
                java.util.UUID.randomUUID().toString(),
                role,
                content,
                null,
                "user".equalsIgnoreCase(role) ? AiMessageType.USER.name() : AiMessageType.TEXT.name(),
                "COMPLETED",
                Map.of(),
                1,
                1,
                updatedAt,
                updatedAt));
    }

    /** @deprecated use round flush instead */
    public void onMessageUpserted(String sessionId, AiSessionStore.ChatMessage message) {
        if (message == null) {
            return;
        }
        onRoundFlushed(sessionId, List.of(message));
    }

    boolean persistenceEnabled() {
        return persistenceEnabled;
    }

    private void ensureSessionHydrated(String sessionId) {
        if (sessionId == null || sessionId.isBlank() || sessionStore.hasSession(sessionId)) {
            return;
        }
        if (!ensurePersistenceReady()) {
            return;
        }
        try {
            ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
            List<ApmConfigRepository.AiMessageRow> rows = repository.loadAiMessages(sessionId);
            if (rows.isEmpty()) {
                return;
            }
            sessionStore.hydrateSession(
                    sessionId,
                    rows.get(rows.size() - 1).agent(),
                    "USER",
                    deriveTitle(rows),
                    null,
                    null,
                    rows.get(rows.size() - 1).userName(),
                    rows.stream().map(this::toChatMessage).toList());
        } catch (Exception e) {
            log.debug("Failed to hydrate AI session {} from store: {}", sessionId, e.getMessage());
        }
    }

    private boolean ensurePersistenceReady() {
        if (persistenceEnabled) {
            return true;
        }
        try {
            if (new ApmConfigRepository(readRepository, configDatabase).aiMessageSchemaReady()) {
                persistenceEnabled = true;
                return true;
            }
        } catch (Exception e) {
            log.debug("AI message schema check failed: {}", e.getMessage());
        }
        return false;
    }

    private ApmConfigRepository.AiMessageRow toMessageRow(
            String sessionId,
            AiSessionStore.ChatMessage message,
            AiSessionStore.SessionSummary summary) {
        String userName = summary == null || summary.userName() == null ? "admin" : summary.userName();
        String agent = message.expertId() != null && !message.expertId().isBlank()
                ? message.expertId()
                : summary == null ? null : summary.expertId();
        Instant createdAt = message.ts() == null ? Instant.now() : message.ts();
        Instant updatedAt = message.updatedAt() == null ? createdAt : message.updatedAt();
        return new ApmConfigRepository.AiMessageRow(
                sessionId,
                message.messageId(),
                "USER",
                userName,
                userName,
                agent,
                "AGENT",
                message.roundIndex(),
                message.messageIndex(),
                message.messageType(),
                message.messageStatus(),
                stringMetadata(message.metadata(), "modelName"),
                resolveCallId(message.metadata()),
                stringMetadata(message.metadata(), "toolName"),
                message.content(),
                writeAttachments(message.metadata()),
                AiMessageType.ERROR.name().equals(message.messageType()) ? message.content() : null,
                writeMetadata(message.metadata()),
                stringMetadata(message.metadata(), "triggerSource"),
                createdAt,
                updatedAt);
    }

    private AiSessionStore.ChatMessage toChatMessage(ApmConfigRepository.AiMessageRow row) {
        Map<String, Object> metadata = new LinkedHashMap<>(parseMetadata(row.metadataJson()));
        mergeAttachments(metadata, row.attachmentsJson());
        if (row.toolName() != null && !row.toolName().isBlank()) {
            metadata = new LinkedHashMap<>(metadata);
            metadata.put("toolName", row.toolName());
        }
        if (row.callId() != null && !row.callId().isBlank()) {
            metadata = new LinkedHashMap<>(metadata);
            metadata.put("callId", row.callId());
            metadata.putIfAbsent("toolCallId", row.callId());
        }
        if (row.modelName() != null && !row.modelName().isBlank()) {
            metadata = new LinkedHashMap<>(metadata);
            metadata.put("modelName", row.modelName());
        }
        return new AiSessionStore.ChatMessage(
                row.messageId(),
                AiSessionStore.roleFromMessageType(row.messageType()),
                row.content(),
                row.agent(),
                row.messageType(),
                row.messageStatus(),
                metadata,
                row.roundIndex(),
                row.messageIndex(),
                row.createdAt(),
                row.updatedAt());
    }

    private static String deriveTitle(List<ApmConfigRepository.AiMessageRow> messages) {
        return messages.stream()
                .filter(row -> AiMessageType.USER.name().equals(row.messageType()))
                .map(ApmConfigRepository.AiMessageRow::content)
                .filter(content -> content != null && !content.isBlank())
                .findFirst()
                .map(content -> {
                    String trimmed = content.trim().replaceAll("\\s+", " ");
                    return trimmed.length() <= 48 ? trimmed : trimmed.substring(0, 48) + "...";
                })
                .orElse(null);
    }

    private static String stringMetadata(Map<String, Object> metadata, String key) {
        if (metadata == null || key == null) {
            return null;
        }
        Object value = metadata.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static String resolveCallId(Map<String, Object> metadata) {
        String callId = stringMetadata(metadata, "callId");
        if (callId != null && !callId.isBlank()) {
            return callId;
        }
        return stringMetadata(metadata, "toolCallId");
    }

    private static Map<String, Object> parseMetadata(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static void mergeAttachments(Map<String, Object> metadata, String attachmentsJson) {
        if (attachmentsJson == null || attachmentsJson.isBlank() || metadata.containsKey("attachments")) {
            return;
        }
        try {
            List<Map<String, Object>> attachments = OBJECT_MAPPER.readValue(
                    attachmentsJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            if (!attachments.isEmpty()) {
                metadata.put("attachments", attachments);
            }
        } catch (Exception ignored) {
            // ignore malformed attachment payload
        }
    }

    private static String writeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(metadata);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String writeAttachments(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        Object attachments = metadata.get("attachments");
        if (attachments == null) {
            attachments = metadata.get("attachmentsJson");
        }
        if (attachments == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attachments);
        } catch (Exception e) {
            return null;
        }
    }
}
