package com.databuff.apm.web.ai.platform.tool;

import java.time.Instant;

public record AiToolDefinition(
        String toolId,
        String name,
        String category,
        String description,
        ToolType type,
        String implementation,
        String schemaJson,
        String configJson,
        boolean enabled,
        boolean builtIn,
        long version,
        Instant createdAt,
        Instant updatedAt) {

    public AiToolDefinition {
        category = normalizeCategory(category);
    }

    public AiToolDefinition withVersion(long nextVersion, Instant updatedAt) {
        return new AiToolDefinition(
                toolId, name, category, description, type, implementation, schemaJson, configJson,
                enabled, builtIn, nextVersion, createdAt, updatedAt);
    }

    private static String normalizeCategory(String value) {
        return value == null || value.isBlank() ? "默认分类" : value.trim();
    }
}
