package com.databuff.apm.web.ai.platform.skill;

import java.time.Instant;

public record AiSkillDefinition(
        String skillId,
        String name,
        String category,
        String description,
        String contentUri,
        String filePath,
        boolean enabled,
        boolean builtIn,
        long version,
        String checksum,
        Instant createdAt,
        Instant updatedAt) {

    public AiSkillDefinition {
        category = normalizeCategory(category);
    }

    public AiSkillDefinition withVersion(long nextVersion, String checksum, Instant updatedAt) {
        return new AiSkillDefinition(
                skillId, name, category, description, contentUri, filePath,
                enabled, builtIn, nextVersion, checksum, createdAt, updatedAt);
    }

    private static String normalizeCategory(String value) {
        return value == null || value.isBlank() ? "默认分类" : value.trim();
    }
}
