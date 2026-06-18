package com.databuff.apm.web.persistence;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.ai.platform.task.ExpertTask;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExpertTaskPersistence {

    private static final Logger log = LoggerFactory.getLogger(ExpertTaskPersistence.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ApmReadRepository readRepository;
    private final String configDatabase;
    private volatile boolean persistenceEnabled;

    public ExpertTaskPersistence(ApmReadRepository readRepository, ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.configDatabase = storageProperties.configDatabase();
    }

    void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.aiExpertTaskSchemaReady()) {
            log.info("AI expert task store not ready; tasks stay in-memory only");
            return;
        }
        persistenceEnabled = true;
        log.info("AI expert task persistence enabled");
    }

    public void persistTask(ExpertTask task) {
        if (!persistenceEnabled || task == null) {
            return;
        }
        try {
            new ApmConfigRepository(readRepository, configDatabase).upsertAiExpertTask(toRow(task));
        } catch (Exception e) {
            log.warn("Failed to persist expert task {}: {}", task.taskId(), e.getMessage());
        }
    }

    private static ApmConfigRepository.AiExpertTaskRow toRow(ExpertTask task) {
        String metadataJson = "{}";
        try {
            metadataJson = OBJECT_MAPPER.writeValueAsString(task.metadata());
        } catch (Exception ignored) {
            // keep default
        }
        return new ApmConfigRepository.AiExpertTaskRow(
                task.taskId(),
                task.parentTaskId(),
                task.sessionId(),
                task.sourceExpertId(),
                task.targetExpertId(),
                task.status().name(),
                task.input(),
                task.output(),
                task.error(),
                metadataJson,
                task.createdAt(),
                task.updatedAt(),
                task.completedAt());
    }
}
