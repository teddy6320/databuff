package com.databuff.apm.web.ai.platform.tool;

import com.databuff.apm.web.ai.platform.expert.AiExpertDefinition;
import com.databuff.apm.web.ai.platform.expert.ExpertToolAccessMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves the exact tool set for one expert runtime. Each expert gets an isolated toolkit;
 * tools are never shared across experts except via the global catalog definition.
 */
@Component
public class ExpertToolResolver {

    private final ToolManagementService toolManagementService;

    public ExpertToolResolver(ToolManagementService toolManagementService) {
        this.toolManagementService = toolManagementService;
    }

    public List<AiToolDefinition> resolve(AiExpertDefinition expert) {
        if (expert == null) {
            return List.of();
        }
        ExpertToolAccessMode mode = expert.options().toolAccessMode();
        if (mode == ExpertToolAccessMode.BLOCKLIST) {
            Set<String> blocked = new HashSet<>(expert.toolIds());
            return toolManagementService.list().stream()
                    .filter(AiToolDefinition::enabled)
                    .filter(tool -> !blocked.contains(tool.toolId()))
                    .sorted(Comparator.comparing(AiToolDefinition::toolId))
                    .toList();
        }
        List<AiToolDefinition> tools = new ArrayList<>();
        for (String toolId : expert.toolIds()) {
            toolManagementService.find(toolId)
                    .filter(AiToolDefinition::enabled)
                    .ifPresent(tools::add);
        }
        return tools;
    }
}
