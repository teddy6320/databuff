package com.databuff.apm.web.ai.platform.runtime;

import com.databuff.apm.web.ai.agent.AgentRuntimeConfig;
import com.databuff.apm.web.ai.platform.skill.AiSkillDefinition;
import com.databuff.apm.web.ai.platform.skill.DeployCommonSkills;
import com.databuff.apm.web.ai.platform.skill.SkillManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
public class SkillFileSyncService {

    private static final Logger log = LoggerFactory.getLogger(SkillFileSyncService.class);

    private final AgentRuntimeConfig agentRuntimeConfig;
    private final SkillManagementService skillManagementService;
    private final ResourceLoader resourceLoader;

    public SkillFileSyncService(
            AgentRuntimeConfig agentRuntimeConfig,
            SkillManagementService skillManagementService,
            ResourceLoader resourceLoader) {
        this.agentRuntimeConfig = agentRuntimeConfig;
        this.skillManagementService = skillManagementService;
        this.resourceLoader = resourceLoader;
    }

    public Path syncSkill(AiSkillDefinition skill) {
        if (skill == null || !skill.enabled()) {
            return null;
        }
        if (DeployCommonSkills.isDeployCommonUri(skill.contentUri())) {
            Path builtin = resolveBuiltinSkillMd(skill.contentUri());
            if (builtin != null && Files.isRegularFile(builtin)) {
                return builtin.getParent();
            }
            log.warn("Built-in skill {} missing under {}", skill.skillId(), agentRuntimeConfig.builtinSkillsDirectory());
            return null;
        }
        Path target = resolveCustomTargetPath(skill);
        Path targetDir = target.getParent();
        try {
            if (Files.isDirectory(targetDir) && Files.isRegularFile(targetDir.resolve("SKILL.md"))) {
                Path source = resolveContentFile(skill.contentUri());
                if (source != null && Files.isRegularFile(source)) {
                    Path sourceDir = source.getParent().normalize();
                    if (sourceDir.equals(targetDir.normalize())) {
                        log.debug("Skill {} package already present at {}", skill.skillId(), targetDir);
                        return targetDir;
                    }
                }
            }
            Files.createDirectories(targetDir);
            String content = readContentUri(skill.contentUri());
            if (content == null || content.isBlank()) {
                log.warn("Skill {} contentUri {} is empty", skill.skillId(), skill.contentUri());
                return null;
            }
            Files.writeString(target, content, StandardCharsets.UTF_8);
            log.debug("Synced skill {} to {}", skill.skillId(), target);
            return targetDir;
        } catch (IOException e) {
            log.warn("Failed to sync skill {}: {}", skill.skillId(), e.getMessage());
            return null;
        }
    }

    public void syncAllEnabledSkills() {
        skillManagementService.list().stream()
                .filter(AiSkillDefinition::enabled)
                .sorted(Comparator.comparing(AiSkillDefinition::skillId))
                .forEach(this::syncSkill);
    }

    public void syncSkillsForExpert(List<String> skillIds) {
        for (String skillId : skillIds) {
            skillManagementService.find(skillId).ifPresent(this::syncSkill);
        }
    }

    public void deleteSyncedSkill(String skillId) {
        if (skillId == null || skillId.isBlank()) {
            return;
        }
        Path targetDir = agentRuntimeConfig.customSkillsDirectory().resolve(skillId);
        try {
            if (Files.isDirectory(targetDir)) {
                try (var paths = Files.walk(targetDir)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete synced skill path {}: {}", path, e.getMessage());
                        }
                    });
                }
            }
        } catch (IOException e) {
            log.warn("Failed to delete synced skill {}: {}", skillId, e.getMessage());
        }
    }

    Path resolveCustomTargetPath(AiSkillDefinition skill) {
        return agentRuntimeConfig.customSkillsDirectory()
                .resolve(skill.skillId())
                .resolve("SKILL.md")
                .normalize();
    }

    public String readSkillContent(AiSkillDefinition skill) throws IOException {
        if (skill == null) {
            return null;
        }
        Path customSkillMd = agentRuntimeConfig.customSkillsDirectory()
                .resolve(skill.skillId())
                .resolve("SKILL.md")
                .normalize();
        if (Files.isRegularFile(customSkillMd)) {
            return Files.readString(customSkillMd, StandardCharsets.UTF_8);
        }
        Path builtinSkillMd = resolveBuiltinSkillMd(skill.contentUri());
        if (builtinSkillMd != null && Files.isRegularFile(builtinSkillMd)) {
            return Files.readString(builtinSkillMd, StandardCharsets.UTF_8);
        }
        return readContentUri(skill.contentUri());
    }

    private Path resolveBuiltinSkillMd(String contentUri) {
        String skillId = DeployCommonSkills.skillIdFromUri(contentUri);
        if (skillId == null) {
            return null;
        }
        return agentRuntimeConfig.builtinSkillsDirectory()
                .resolve(skillId)
                .resolve("SKILL.md")
                .normalize();
    }

    private Path resolveContentFile(String contentUri) throws IOException {
        if (contentUri == null || contentUri.isBlank()) {
            return null;
        }
        String trimmed = contentUri.trim();
        if (DeployCommonSkills.isDeployCommonUri(trimmed)) {
            return resolveBuiltinSkillMd(trimmed);
        }
        if (trimmed.startsWith("classpath:") || trimmed.startsWith("file:")) {
            Resource resource = resourceLoader.getResource(trimmed);
            if (!resource.exists()) {
                return null;
            }
            return resource.getFile().toPath().normalize();
        }
        Path path = Path.of(trimmed);
        if (Files.isRegularFile(path)) {
            return path.normalize();
        }
        return null;
    }

    private String readContentUri(String contentUri) throws IOException {
        if (contentUri == null || contentUri.isBlank()) {
            return null;
        }
        String trimmed = contentUri.trim();
        if (DeployCommonSkills.isDeployCommonUri(trimmed)) {
            Path builtin = resolveBuiltinSkillMd(trimmed);
            if (builtin != null && Files.isRegularFile(builtin)) {
                return Files.readString(builtin, StandardCharsets.UTF_8);
            }
            return null;
        }
        if (trimmed.startsWith("classpath:")) {
            Resource resource = resourceLoader.getResource(trimmed);
            if (!resource.exists()) {
                return null;
            }
            try (InputStream input = resource.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        if (trimmed.startsWith("file:")) {
            Resource resource = resourceLoader.getResource(trimmed);
            try (InputStream input = resource.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        Path path = Path.of(trimmed);
        if (Files.isRegularFile(path)) {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        return null;
    }
}
