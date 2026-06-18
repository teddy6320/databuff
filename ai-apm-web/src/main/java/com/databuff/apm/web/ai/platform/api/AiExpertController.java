package com.databuff.apm.web.ai.platform.api;

import com.databuff.apm.web.ai.platform.AiPlatformApiException;
import com.databuff.apm.web.ai.platform.expert.AiExpertDefinition;
import com.databuff.apm.web.ai.platform.expert.ExpertManagementService;
import com.databuff.apm.web.ai.platform.expert.ExpertRuntimeOptions;
import com.databuff.apm.web.ai.platform.expert.ExpertType;
import com.databuff.apm.web.ai.platform.runtime.ExpertChatInput;
import com.databuff.apm.web.ai.platform.runtime.ExpertChatResult;
import com.databuff.apm.web.ai.platform.runtime.ExpertRuntimeRegistry;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/experts")
public class AiExpertController {

    private final ExpertManagementService expertManagementService;
    private final ExpertRuntimeRegistry expertRuntimeRegistry;

    public AiExpertController(
            ExpertManagementService expertManagementService,
            ExpertRuntimeRegistry expertRuntimeRegistry) {
        this.expertManagementService = expertManagementService;
        this.expertRuntimeRegistry = expertRuntimeRegistry;
    }

    @GetMapping
    public List<AiExpertDefinition> list() {
        return expertManagementService.list();
    }

    @GetMapping("/{expertId}")
    public AiExpertDefinition get(@PathVariable String expertId) {
        return expertManagementService.find(expertId)
                .orElseThrow(() -> AiPlatformApiException.notFound("expert", expertId));
    }

    @PostMapping
    public AiExpertDefinition create(@RequestBody SaveExpertRequest request) {
        if (request == null || blank(request.expertId())) {
            throw AiPlatformApiException.badRequest("expertId is required");
        }
        if (expertManagementService.find(request.expertId()).isPresent()) {
            throw AiPlatformApiException.conflict("expert_exists", "expert already exists: " + request.expertId());
        }
        return expertManagementService.save(toDefinition(request, Instant.now()));
    }

    @PutMapping("/{expertId}")
    public AiExpertDefinition update(@PathVariable String expertId, @RequestBody SaveExpertRequest request) {
        AiExpertDefinition existing = expertManagementService.find(expertId)
                .orElseThrow(() -> AiPlatformApiException.notFound("expert", expertId));
        SaveExpertRequest merged = request == null ? new SaveExpertRequest(
                expertId, existing.name(), existing.category(), existing.description(), existing.type(),
                existing.modelProviderCode(), existing.modelName(), existing.systemPrompt(),
                existing.toolIds(), existing.skillIds(), existing.options(), existing.enabled())
                : request.withExpertId(expertId);
        return expertManagementService.save(toDefinition(merged, existing.createdAt()));
    }

    @DeleteMapping("/{expertId}")
    public Map<String, Boolean> delete(@PathVariable String expertId) {
        if (!expertManagementService.delete(expertId)) {
            throw AiPlatformApiException.conflict("expert_protected", "built-in expert cannot be deleted: " + expertId);
        }
        return Map.of("deleted", true);
    }

    @PostMapping("/{expertId}/enable")
    public AiExpertDefinition enable(@PathVariable String expertId) {
        return setEnabled(expertId, true);
    }

    @PostMapping("/{expertId}/disable")
    public AiExpertDefinition disable(@PathVariable String expertId) {
        return setEnabled(expertId, false);
    }

    @PostMapping("/{expertId}/debug")
    public Map<String, Object> debug(@PathVariable String expertId, @RequestBody DebugExpertRequest request) {
        if (request == null || blank(request.message())) {
            throw AiPlatformApiException.badRequest("message is required");
        }
        AiExpertDefinition expert = expertManagementService.find(expertId)
                .orElseThrow(() -> AiPlatformApiException.notFound("expert", expertId));
        if (!expert.enabled()) {
            throw AiPlatformApiException.conflict("expert_disabled", "expert is disabled: " + expertId);
        }
        try {
            ExpertChatResult result = expertRuntimeRegistry.getOrCreate(expertId)
                    .chat(ExpertChatInput.of(request.message()))
                    .block(Duration.ofSeconds(30));
            if (result == null) {
                return Map.of("expertId", expertId, "ok", false, "error", "empty runtime response");
            }
            return Map.of(
                    "expertId", expertId,
                    "ok", result.ok(),
                    "reply", result.ok() ? result.content() : "",
                    "error", result.ok() ? "" : result.error());
        } catch (Exception e) {
            return Map.of("expertId", expertId, "ok", false, "error", e.getMessage() == null ? "debug failed" : e.getMessage());
        }
    }

    @PostMapping("/{expertId}/reload")
    public Map<String, Object> reload(@PathVariable String expertId) {
        expertManagementService.find(expertId)
                .orElseThrow(() -> AiPlatformApiException.notFound("expert", expertId));
        expertRuntimeRegistry.invalidate(expertId, "manual reload");
        return Map.of("expertId", expertId, "reloaded", true);
    }

    private AiExpertDefinition setEnabled(String expertId, boolean enabled) {
        AiExpertDefinition existing = expertManagementService.find(expertId)
                .orElseThrow(() -> AiPlatformApiException.notFound("expert", expertId));
        return expertManagementService.save(new AiExpertDefinition(
                existing.expertId(), existing.name(), existing.category(), existing.description(), existing.type(),
                existing.modelProviderCode(), existing.modelName(), existing.systemPrompt(),
                existing.toolIds(), existing.skillIds(), existing.options(),
                enabled, existing.builtIn(), existing.version(), existing.createdAt(), Instant.now()));
    }

    private AiExpertDefinition toDefinition(SaveExpertRequest request, Instant createdAt) {
        ExpertType type = request.type() == null ? ExpertType.CUSTOM : request.type();
        Instant now = Instant.now();
        return new AiExpertDefinition(
                request.expertId(),
                request.name(),
                normalizeCategory(request.category()),
                request.description(),
                type,
                request.modelProviderCode(),
                request.modelName(),
                request.systemPrompt(),
                request.toolIds() == null ? List.of() : request.toolIds(),
                request.skillIds() == null ? List.of() : request.skillIds(),
                request.options() == null ? ExpertRuntimeOptions.defaults() : request.options(),
                request.enabled() == null || request.enabled(),
                false,
                0,
                createdAt == null ? now : createdAt,
                now);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record SaveExpertRequest(
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
            Boolean enabled) {

        SaveExpertRequest withExpertId(String nextExpertId) {
            return new SaveExpertRequest(
                    nextExpertId, name, category, description, type, modelProviderCode, modelName, systemPrompt,
                    toolIds, skillIds, options, enabled);
        }
    }

    private static String normalizeCategory(String category) {
        return category == null || category.isBlank() ? "默认分类" : category.trim();
    }

    public record DebugExpertRequest(String message) {
    }
}
