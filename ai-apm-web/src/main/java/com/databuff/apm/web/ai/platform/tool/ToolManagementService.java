package com.databuff.apm.web.ai.platform.tool;

import com.databuff.apm.web.ai.platform.BuiltInExpertCatalog;
import com.databuff.apm.web.ai.platform.runtime.ExpertRuntimeRegistry;
import com.databuff.apm.web.persistence.AiPlatformPersistence;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ToolManagementService {

    @Autowired
    private ObjectProvider<AiPlatformPersistence> persistence;
    @Autowired
    private ObjectProvider<ExpertRuntimeRegistry> runtimeRegistry;
    private final ConcurrentMap<String, AiToolDefinition> tools = new ConcurrentHashMap<>();

    @PostConstruct
    void initDefaults() {
        BuiltInExpertCatalog.tools().forEach(tool -> tools.put(tool.toolId(), tool));
    }

    public List<AiToolDefinition> list() {
        return tools.values().stream()
                .sorted(Comparator.comparing(AiToolDefinition::toolId))
                .toList();
    }

    public Optional<AiToolDefinition> find(String toolId) {
        return Optional.ofNullable(tools.get(toolId));
    }

    public AiToolDefinition save(AiToolDefinition definition) {
        validate(definition);
        Instant now = Instant.now();
        AiToolDefinition saved = tools.compute(definition.toolId(), (id, existing) -> {
            if (existing == null) {
                long version = definition.version() <= 0 ? 1L : definition.version();
                return new AiToolDefinition(
                        definition.toolId(), definition.name(), definition.category(), definition.description(), definition.type(),
                        definition.implementation(), definition.schemaJson(), definition.configJson(),
                        definition.enabled(), definition.builtIn(), version, now, now);
            }
            return new AiToolDefinition(
                    definition.toolId(), definition.name(), definition.category(), definition.description(), definition.type(),
                    definition.implementation(), definition.schemaJson(), definition.configJson(),
                    definition.enabled(), existing.builtIn(), existing.version() + 1, existing.createdAt(), now);
        });
        ifAvailable(sync -> sync.persistTool(saved));
        invalidateByTool(saved.toolId());
        return saved;
    }

    public boolean delete(String toolId) {
        AiToolDefinition existing = tools.get(toolId);
        if (existing == null || existing.builtIn()) {
            return false;
        }
        boolean removed = tools.remove(toolId) != null;
        if (removed) {
            ifAvailable(sync -> sync.deleteTool(toolId));
            invalidateByTool(toolId);
        }
        return removed;
    }

    public boolean existsEnabled(String toolId) {
        AiToolDefinition tool = tools.get(toolId);
        return tool != null && tool.enabled();
    }

    public boolean exists(String toolId) {
        return tools.containsKey(toolId);
    }

    public void applyPersistedRows(List<AiToolDefinition> definitions) {
        for (AiToolDefinition definition : definitions) {
            validate(definition);
            tools.compute(definition.toolId(), (id, existing) -> new AiToolDefinition(
                    definition.toolId(), definition.name(), definition.category(), definition.description(), definition.type(),
                    definition.implementation(), definition.schemaJson(), definition.configJson(),
                    definition.enabled(), (existing != null && existing.builtIn()) || definition.builtIn(),
                    definition.version(), definition.createdAt(), definition.updatedAt()));
        }
    }

    private static void validate(AiToolDefinition definition) {
        if (definition == null || blank(definition.toolId()) || blank(definition.name())) {
            throw new IllegalArgumentException("toolId and name are required");
        }
        if (definition.type() == null) {
            throw new IllegalArgumentException("tool type is required");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private void ifAvailable(java.util.function.Consumer<AiPlatformPersistence> consumer) {
        if (persistence != null) {
            persistence.ifAvailable(consumer);
        }
    }

    private void invalidateByTool(String toolId) {
        if (runtimeRegistry != null) {
            runtimeRegistry.ifAvailable(registry -> registry.invalidateByTool(toolId));
        }
    }
}
