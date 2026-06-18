package com.databuff.apm.web.ai.platform.runtime;

import com.databuff.apm.web.ai.platform.skill.AiSkillDefinition;
import com.databuff.apm.web.ai.platform.expert.AiExpertDefinition;
import com.databuff.apm.web.ai.platform.tool.AiToolDefinition;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record RuntimeCacheKey(
        String expertId,
        long expertVersion,
        String toolVersionsHash,
        String skillChecksumsHash,
        String providerKey,
        String routingCatalogHash) {

    public String fingerprint() {
        return expertId + "|" + expertVersion + "|" + toolVersionsHash + "|"
                + skillChecksumsHash + "|" + providerKey + "|" + routingCatalogHash;
    }

    public static RuntimeCacheKey of(
            AiExpertDefinition expert,
            List<AiToolDefinition> tools,
            List<AiSkillDefinition> skills,
            String providerCode,
            long providerVersion) {
        return of(expert, tools, skills, providerCode, providerVersion, "");
    }

    public static RuntimeCacheKey of(
            AiExpertDefinition expert,
            List<AiToolDefinition> tools,
            List<AiSkillDefinition> skills,
            String providerCode,
            long providerVersion,
            String routingCatalogHash) {
        return new RuntimeCacheKey(
                expert.expertId(),
                expert.version(),
                hashTools(expert, tools),
                hashSkills(skills),
                (providerCode == null ? "default" : providerCode) + ":" + providerVersion,
                routingCatalogHash == null ? "" : routingCatalogHash);
    }

    private static String hashTools(AiExpertDefinition expert, List<AiToolDefinition> tools) {
        String mode = expert.options().toolAccessMode() == null
                ? "ALLOWLIST"
                : expert.options().toolAccessMode().name();
        String selected = expert.toolIds().stream().sorted().reduce((a, b) -> a + "," + b).orElse("");
        String resolved = tools.stream()
                .sorted(Comparator.comparing(AiToolDefinition::toolId))
                .map(tool -> tool.toolId() + "@" + tool.version())
                .collect(Collectors.joining(","));
        return mode + "|" + selected + "=>" + resolved;
    }

    private static String hashSkills(List<AiSkillDefinition> skills) {
        return skills.stream()
                .sorted(Comparator.comparing(AiSkillDefinition::skillId))
                .map(skill -> skill.skillId() + "@" + skill.version() + ":" + nullSafe(skill.checksum()))
                .collect(Collectors.joining(","));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
