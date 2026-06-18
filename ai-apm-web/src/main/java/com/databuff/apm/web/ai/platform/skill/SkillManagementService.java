package com.databuff.apm.web.ai.platform.skill;

import com.databuff.apm.web.ai.platform.BuiltInExpertCatalog;
import com.databuff.apm.web.ai.platform.runtime.ExpertRuntimeRegistry;
import com.databuff.apm.web.ai.platform.runtime.SkillFileSyncService;
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
public class SkillManagementService {

    @Autowired
    private ObjectProvider<AiPlatformPersistence> persistence;
    @Autowired
    private ObjectProvider<ExpertRuntimeRegistry> runtimeRegistry;
    @Autowired
    private ObjectProvider<SkillFileSyncService> skillFileSyncService;
    private final ConcurrentMap<String, AiSkillDefinition> skills = new ConcurrentHashMap<>();

    @PostConstruct
    void initDefaults() {
        BuiltInExpertCatalog.skills().forEach(skill -> skills.put(skill.skillId(), skill));
    }

    public List<AiSkillDefinition> list() {
        return skills.values().stream()
                .sorted(Comparator.comparing(AiSkillDefinition::skillId))
                .toList();
    }

    public Optional<AiSkillDefinition> find(String skillId) {
        return Optional.ofNullable(skills.get(skillId));
    }

    public AiSkillDefinition save(AiSkillDefinition definition) {
        validate(definition);
        Instant now = Instant.now();
        AiSkillDefinition saved = skills.compute(definition.skillId(), (id, existing) -> {
            if (existing == null) {
                long version = definition.version() <= 0 ? 1L : definition.version();
                return new AiSkillDefinition(
                        definition.skillId(), definition.name(), definition.category(), definition.description(),
                        definition.contentUri(), definition.filePath(), definition.enabled(), definition.builtIn(),
                        version, definition.checksum(), now, now);
            }
            return new AiSkillDefinition(
                    definition.skillId(), definition.name(), definition.category(), definition.description(),
                    definition.contentUri(), definition.filePath(), definition.enabled(), existing.builtIn(),
                    existing.version() + 1, definition.checksum(), existing.createdAt(), now);
        });
        ifAvailable(sync -> sync.persistSkill(saved));
        syncSkillFile(saved);
        invalidateBySkill(saved.skillId());
        return saved;
    }

    public boolean delete(String skillId) {
        AiSkillDefinition existing = skills.get(skillId);
        if (existing == null || existing.builtIn()) {
            return false;
        }
        boolean removed = skills.remove(skillId) != null;
        if (removed) {
            ifAvailable(sync -> sync.deleteSkill(skillId));
            deleteSyncedSkillFile(skillId);
            invalidateBySkill(skillId);
        }
        return removed;
    }

    public boolean existsEnabled(String skillId) {
        AiSkillDefinition skill = skills.get(skillId);
        return skill != null && skill.enabled();
    }

    public void applyPersistedRows(List<AiSkillDefinition> definitions) {
        for (AiSkillDefinition definition : definitions) {
            validate(definition);
            skills.compute(definition.skillId(), (id, existing) -> new AiSkillDefinition(
                    definition.skillId(), definition.name(), definition.category(), definition.description(),
                    definition.contentUri(), definition.filePath(), definition.enabled(),
                    (existing != null && existing.builtIn()) || definition.builtIn(),
                    definition.version(), definition.checksum(), definition.createdAt(), definition.updatedAt()));
        }
    }

    private static void validate(AiSkillDefinition definition) {
        if (definition == null || blank(definition.skillId()) || blank(definition.name())) {
            throw new IllegalArgumentException("skillId and name are required");
        }
        if (blank(definition.contentUri())) {
            throw new IllegalArgumentException("skill contentUri is required");
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

    private void invalidateBySkill(String skillId) {
        if (runtimeRegistry != null) {
            runtimeRegistry.ifAvailable(registry -> registry.invalidateBySkill(skillId));
        }
    }

    private void syncSkillFile(AiSkillDefinition skill) {
        if (skillFileSyncService != null) {
            skillFileSyncService.ifAvailable(sync -> sync.syncSkill(skill));
        }
    }

    private void deleteSyncedSkillFile(String skillId) {
        if (skillFileSyncService != null) {
            skillFileSyncService.ifAvailable(sync -> sync.deleteSyncedSkill(skillId));
        }
    }
}
