package com.databuff.apm.web.ai.platform.runtime;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.AgentSkillRepositoryInfo;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Merges multiple on-disk skill roots (custom imports first, then deploy/common builtins). */
public final class LayeredFilesystemSkillRepository implements AgentSkillRepository {

    private final List<FileSystemSkillRepository> layers;

    public LayeredFilesystemSkillRepository(List<Path> directories) {
        List<FileSystemSkillRepository> repos = new ArrayList<>();
        if (directories != null) {
            for (Path directory : directories) {
                if (directory != null && Files.isDirectory(directory)) {
                    repos.add(new FileSystemSkillRepository(directory));
                }
            }
        }
        this.layers = List.copyOf(repos);
    }

    @Override
    public AgentSkill getSkill(String skillId) {
        for (FileSystemSkillRepository layer : layers) {
            AgentSkill skill = layer.getSkill(skillId);
            if (skill != null) {
                return skill;
            }
        }
        return null;
    }

    @Override
    public List<String> getAllSkillNames() {
        Set<String> names = new LinkedHashSet<>();
        for (FileSystemSkillRepository layer : layers) {
            names.addAll(layer.getAllSkillNames());
        }
        return List.copyOf(names);
    }

    @Override
    public List<AgentSkill> getAllSkills() {
        List<AgentSkill> skills = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (FileSystemSkillRepository layer : layers) {
            for (AgentSkill skill : layer.getAllSkills()) {
                if (skill == null) {
                    continue;
                }
                String skillId = skill.getSkillId() != null && !skill.getSkillId().isBlank()
                        ? skill.getSkillId()
                        : skill.getName();
                if (skillId == null || skillId.isBlank() || seen.contains(skillId)) {
                    continue;
                }
                seen.add(skillId);
                skills.add(skill);
            }
        }
        return skills;
    }

    @Override
    public boolean save(List<AgentSkill> skills, boolean overwrite) {
        if (layers.isEmpty()) {
            return false;
        }
        return layers.get(0).save(skills, overwrite);
    }

    @Override
    public boolean delete(String skillId) {
        for (FileSystemSkillRepository layer : layers) {
            if (layer.skillExists(skillId) && layer.delete(skillId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean skillExists(String skillId) {
        for (FileSystemSkillRepository layer : layers) {
            if (layer.skillExists(skillId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AgentSkillRepositoryInfo getRepositoryInfo() {
        return layers.isEmpty() ? null : layers.get(0).getRepositoryInfo();
    }

    @Override
    public String getSource() {
        return layers.isEmpty() ? "" : layers.get(0).getSource();
    }

    @Override
    public void setWriteable(boolean writeable) {
        if (!layers.isEmpty()) {
            layers.get(0).setWriteable(writeable);
        }
    }

    @Override
    public boolean isWriteable() {
        return !layers.isEmpty() && layers.get(0).isWriteable();
    }
}
