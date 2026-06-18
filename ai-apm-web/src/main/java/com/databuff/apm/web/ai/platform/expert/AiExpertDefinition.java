package com.databuff.apm.web.ai.platform.expert;

import java.time.Instant;
import java.util.List;

public record AiExpertDefinition(
        String expertId,
        String name,
        String category,
        String description,
        ExpertType type,
        String modelProviderCode,
        String modelName,
        String systemPrompt,
        List<String> toolIds,
        List<String> skillIds,
        ExpertRuntimeOptions options,
        boolean enabled,
        boolean builtIn,
        long version,
        Instant createdAt,
        Instant updatedAt) {

    public AiExpertDefinition {
        category = normalizeCategory(category);
        toolIds = toolIds == null ? List.of() : List.copyOf(toolIds);
        skillIds = skillIds == null ? List.of() : List.copyOf(skillIds);
        options = options == null ? ExpertRuntimeOptions.defaults() : options;
    }

    public AiExpertDefinition withVersion(long nextVersion, Instant updatedAt) {
        return new AiExpertDefinition(
                expertId, name, category, description, type, modelProviderCode, modelName, systemPrompt,
                toolIds, skillIds, options, enabled, builtIn, nextVersion, createdAt, updatedAt);
    }

    private static String normalizeCategory(String value) {
        return value == null || value.isBlank() ? "默认分类" : value.trim();
    }
}
