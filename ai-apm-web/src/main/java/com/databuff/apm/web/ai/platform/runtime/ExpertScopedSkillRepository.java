package com.databuff.apm.web.ai.platform.runtime;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.AgentSkillRepositoryInfo;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Limits a delegate skill repository to the skill IDs bound to one expert.
 * The runtime skills directory is shared across experts, so filtering is required
 * to avoid exposing unrelated synced skills.
 */
public final class ExpertScopedSkillRepository implements AgentSkillRepository {

    private final AgentSkillRepository delegate;
    private final Set<String> allowedSkillIds;

    public ExpertScopedSkillRepository(AgentSkillRepository delegate, Collection<String> allowedSkillIds) {
        this.delegate = delegate;
        this.allowedSkillIds = allowedSkillIds == null || allowedSkillIds.isEmpty()
                ? Set.of()
                : Set.copyOf(allowedSkillIds);
    }

    @Override
    public AgentSkill getSkill(String skillId) {
        if (!isAllowed(skillId)) {
            return null;
        }
        return delegate.getSkill(skillId);
    }

    @Override
    public List<String> getAllSkillNames() {
        return delegate.getAllSkillNames().stream()
                .filter(this::isAllowed)
                .toList();
    }

    @Override
    public List<AgentSkill> getAllSkills() {
        List<AgentSkill> loaded = delegate.getAllSkills().stream()
                .filter(skill -> skill != null && isAllowed(resolveSkillId(skill)))
                .toList();
        if (!loaded.isEmpty()) {
            return loaded;
        }
        return getAllSkillNames().stream()
                .map(this::getSkill)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public boolean save(List<AgentSkill> skills, boolean overwrite) {
        return delegate.save(skills, overwrite);
    }

    @Override
    public boolean delete(String skillId) {
        if (!isAllowed(skillId)) {
            return false;
        }
        return delegate.delete(skillId);
    }

    @Override
    public boolean skillExists(String skillId) {
        return isAllowed(skillId) && delegate.skillExists(skillId);
    }

    @Override
    public AgentSkillRepositoryInfo getRepositoryInfo() {
        return delegate.getRepositoryInfo();
    }

    @Override
    public String getSource() {
        return delegate.getSource();
    }

    @Override
    public void setWriteable(boolean writeable) {
        delegate.setWriteable(writeable);
    }

    @Override
    public boolean isWriteable() {
        return delegate.isWriteable();
    }

    private boolean isAllowed(String skillId) {
        return skillId != null && !skillId.isBlank() && allowedSkillIds.contains(skillId);
    }

    private static String resolveSkillId(AgentSkill skill) {
        String skillId = skill.getSkillId();
        if (skillId != null && !skillId.isBlank()) {
            return skillId;
        }
        return skill.getName();
    }
}
