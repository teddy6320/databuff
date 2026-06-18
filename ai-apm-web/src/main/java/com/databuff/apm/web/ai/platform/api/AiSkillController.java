package com.databuff.apm.web.ai.platform.api;

import com.databuff.apm.web.ai.platform.expert.AiExpertDefinition;
import com.databuff.apm.web.ai.platform.AiPlatformApiException;
import com.databuff.apm.web.ai.platform.expert.ExpertManagementService;
import com.databuff.apm.web.ai.platform.skill.AiSkillDefinition;
import com.databuff.apm.web.ai.platform.skill.SkillManagementService;
import com.databuff.apm.web.ai.platform.skill.SkillPackageService;
import com.databuff.apm.web.ai.platform.runtime.SkillFileSyncService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/ai/skills")
public class AiSkillController {

    private static final Pattern SKILL_ID_PATTERN = Pattern.compile("^[a-z][a-z0-9._-]{1,63}$");
    private static final int MAX_CONTENT_URI_LENGTH = 512;

    private final SkillManagementService skillManagementService;
    private final ExpertManagementService expertManagementService;
    private final SkillFileSyncService skillFileSyncService;
    private final SkillPackageService skillPackageService;

    public AiSkillController(
            SkillManagementService skillManagementService,
            ExpertManagementService expertManagementService,
            SkillFileSyncService skillFileSyncService,
            SkillPackageService skillPackageService) {
        this.skillManagementService = skillManagementService;
        this.expertManagementService = expertManagementService;
        this.skillFileSyncService = skillFileSyncService;
        this.skillPackageService = skillPackageService;
    }

    @GetMapping
    public List<AiSkillDefinition> list() {
        return skillManagementService.list();
    }

    @GetMapping("/{skillId}")
    public AiSkillDefinition get(@PathVariable String skillId) {
        return skillManagementService.find(skillId)
                .orElseThrow(() -> AiPlatformApiException.notFound("skill", skillId));
    }

    @GetMapping("/{skillId}/references")
    public Map<String, Object> references(@PathVariable String skillId) {
        skillManagementService.find(skillId)
                .orElseThrow(() -> AiPlatformApiException.notFound("skill", skillId));
        List<String> expertIds = expertManagementService.listExpertIdsReferencingSkill(skillId);
        return Map.of("skillId", skillId, "expertIds", expertIds);
    }

    @GetMapping("/{skillId}/content")
    public Map<String, Object> content(@PathVariable String skillId) {
        AiSkillDefinition skill = skillManagementService.find(skillId)
                .orElseThrow(() -> AiPlatformApiException.notFound("skill", skillId));
        try {
            String markdown = skillFileSyncService.readSkillContent(skill);
            if (markdown == null || markdown.isBlank()) {
                throw AiPlatformApiException.badRequest("skill content is empty: " + skill.contentUri());
            }
            return Map.of(
                    "skillId", skillId,
                    "contentUri", skill.contentUri(),
                    "markdown", markdown);
        } catch (IOException e) {
            throw AiPlatformApiException.badRequest("failed to read skill content: " + e.getMessage());
        }
    }

    @GetMapping("/{skillId}/files")
    public List<SkillPackageService.SkillFileEntry> files(@PathVariable String skillId) {
        return skillPackageService.listFiles(skillId);
    }

    @GetMapping("/{skillId}/files/content")
    public SkillPackageService.SkillFileContent fileContent(
            @PathVariable String skillId,
            @RequestParam("path") String path) {
        return skillPackageService.readFile(skillId, path);
    }

    @PostMapping("/import/preview")
    public SkillPackageService.SkillImportPreview previewImport(@RequestParam("file") MultipartFile file) {
        return skillPackageService.previewZip(file);
    }

    @PostMapping("/import")
    public AiSkillDefinition importSkill(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "enabled", required = false) Boolean enabled) {
        return skillPackageService.importZip(file, name, category, description, enabled);
    }

    @PostMapping
    public AiSkillDefinition create(@RequestBody SaveSkillRequest request) {
        if (request == null || blank(request.skillId())) {
            throw AiPlatformApiException.badRequest("skillId is required");
        }
        if (skillManagementService.find(request.skillId()).isPresent()) {
            throw AiPlatformApiException.conflict("skill_exists", "skill already exists: " + request.skillId());
        }
        validateSkillRequest(request);
        return skillManagementService.save(toDefinition(request, Instant.now()));
    }

    @PutMapping("/{skillId}")
    public AiSkillDefinition update(@PathVariable String skillId, @RequestBody SaveSkillRequest request) {
        AiSkillDefinition existing = skillManagementService.find(skillId)
                .orElseThrow(() -> AiPlatformApiException.notFound("skill", skillId));
        SaveSkillRequest merged = request == null ? new SaveSkillRequest(
                skillId, existing.name(), existing.category(), existing.description(), existing.contentUri(),
                existing.filePath(), existing.enabled())
                : request.withSkillId(skillId);
        validateSkillRequest(merged);
        return skillManagementService.save(toDefinition(merged, existing.createdAt()));
    }

    @DeleteMapping("/{skillId}")
    public Map<String, Boolean> delete(@PathVariable String skillId) {
        ensureNotReferenced(skillId);
        if (!skillManagementService.delete(skillId)) {
            throw AiPlatformApiException.conflict("skill_protected", "built-in skill cannot be deleted: " + skillId);
        }
        return Map.of("deleted", true);
    }

    @PostMapping("/{skillId}/validate")
    public Map<String, Object> validate(@PathVariable String skillId, @RequestBody(required = false) SaveSkillRequest request) {
        AiSkillDefinition existing = skillManagementService.find(skillId).orElse(null);
        SaveSkillRequest candidate = request == null && existing != null
                ? new SaveSkillRequest(
                        existing.skillId(), existing.name(), existing.category(), existing.description(),
                        existing.contentUri(), existing.filePath(), existing.enabled())
                : request == null ? new SaveSkillRequest(skillId, "name", "默认分类", "desc", "classpath:/ai/skills/x/SKILL.md", null, true)
                : request.withSkillId(skillId);
        validateSkillRequest(candidate);
        return Map.of("skillId", skillId, "valid", true);
    }

    @PostMapping("/{skillId}/enable")
    public AiSkillDefinition enable(@PathVariable String skillId) {
        return setEnabled(skillId, true);
    }

    @PostMapping("/{skillId}/disable")
    public AiSkillDefinition disable(@PathVariable String skillId) {
        return setEnabled(skillId, false);
    }

    private AiSkillDefinition setEnabled(String skillId, boolean enabled) {
        AiSkillDefinition existing = skillManagementService.find(skillId)
                .orElseThrow(() -> AiPlatformApiException.notFound("skill", skillId));
        if (!enabled) {
            List<String> affected = expertManagementService.list().stream()
                    .filter(expert -> expert.enabled() && expert.skillIds().contains(skillId))
                    .map(AiExpertDefinition::expertId)
                    .toList();
            if (!affected.isEmpty()) {
                throw AiPlatformApiException.conflict(
                        "skill_in_use",
                        "skill is referenced by enabled experts: " + String.join(", ", affected));
            }
        }
        return skillManagementService.save(new AiSkillDefinition(
                existing.skillId(), existing.name(), existing.category(), existing.description(),
                existing.contentUri(), existing.filePath(), enabled, existing.builtIn(),
                existing.version(), existing.checksum(), existing.createdAt(), Instant.now()));
    }

    private void ensureNotReferenced(String skillId) {
        boolean referenced = expertManagementService.list().stream()
                .anyMatch(expert -> expert.skillIds().contains(skillId));
        if (referenced) {
            throw AiPlatformApiException.conflict(
                    "skill_in_use", "skill is referenced by one or more experts: " + skillId);
        }
    }

    private static void validateSkillRequest(SaveSkillRequest request) {
        if (blank(request.skillId()) || blank(request.name())) {
            throw AiPlatformApiException.badRequest("skillId and name are required");
        }
        if (!SKILL_ID_PATTERN.matcher(request.skillId()).matches()) {
            throw AiPlatformApiException.badRequest("invalid skillId format: " + request.skillId());
        }
        if (blank(request.contentUri())) {
            throw AiPlatformApiException.badRequest("contentUri is required");
        }
        if (request.contentUri().length() > MAX_CONTENT_URI_LENGTH) {
            throw AiPlatformApiException.badRequest("contentUri exceeds max length");
        }
        if (request.contentUri().contains("..")) {
            throw AiPlatformApiException.badRequest("contentUri must not contain '..'");
        }
    }

    private AiSkillDefinition toDefinition(SaveSkillRequest request, Instant createdAt) {
        Instant now = Instant.now();
        String filePath = request.filePath() == null || request.filePath().isBlank()
                ? request.contentUri()
                : request.filePath();
        return new AiSkillDefinition(
                request.skillId(),
                request.name(),
                normalizeCategory(request.category()),
                request.description(),
                request.contentUri(),
                filePath,
                request.enabled() == null || request.enabled(),
                false,
                0,
                "",
                createdAt == null ? now : createdAt,
                now);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record SaveSkillRequest(
            String skillId,
            String name,
            String category,
            String description,
            String contentUri,
            String filePath,
            Boolean enabled) {

        SaveSkillRequest withSkillId(String nextSkillId) {
            return new SaveSkillRequest(
                    nextSkillId, name, category, description, contentUri, filePath, enabled);
        }
    }

    private static String normalizeCategory(String category) {
        return category == null || category.isBlank() ? "默认分类" : category.trim();
    }
}
