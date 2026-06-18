package com.databuff.apm.web.ai.platform.runtime;

import com.databuff.apm.web.ai.TestAiSupport;
import com.databuff.apm.web.ai.UpdateLlmProviderRequest;
import com.databuff.apm.web.ai.platform.expert.AiExpertDefinition;
import com.databuff.apm.web.ai.platform.expert.ExpertManagementService;
import com.databuff.apm.web.ai.platform.skill.AiSkillDefinition;
import com.databuff.apm.web.ai.platform.skill.SkillManagementService;
import com.databuff.apm.web.ai.tool.ApmToolkit;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CustomSkillExpertIntegrationTest {

    @TempDir
    Path tempDir;

    private AgentScopeRuntimeAdapter adapter;
    private ExpertManagementService expertManagementService;
    private SkillManagementService skillManagementService;
    private SkillFileSyncService skillFileSyncService;

    @BeforeEach
    void setUp() {
        TestAiSupport.AiFixture aiFixture = TestAiSupport.aiFixture();
        aiFixture.agentRuntimeConfig().setCustomSkillsDir(tempDir.toString());
        aiFixture.store().updateProvider("openai", new UpdateLlmProviderRequest(
                null, "sk-test", null, true));
        TestAiSupport.PlatformRuntimeFixture fixture =
                aiFixture.buildPlatformRuntime(mock(ApmToolkit.class));
        adapter = fixture.runtimeAdapter();
        expertManagementService = fixture.expertManagementService();
        skillManagementService = fixture.skillManagementService();
        skillFileSyncService = new SkillFileSyncService(
                aiFixture.agentRuntimeConfig(),
                skillManagementService,
                new org.springframework.core.io.DefaultResourceLoader());
    }

    @Test
    void expertRuntimeLoadsUserCreatedSkillBoundViaExpertSave() throws Exception {
        String markdown = """
                ---
                name: skill.custom.demo
                description: Custom demo skill
                ---
                Follow this custom skill when answering.
                """;
        Path source = tempDir.resolve("source-skill.custom.demo.md");
        Files.writeString(source, markdown);

        skillManagementService.save(new AiSkillDefinition(
                "skill.custom.demo",
                "Custom Demo",
                "测试分类",
                "Custom demo skill",
                "file:" + source.toAbsolutePath(),
                "./data/skills/skill.custom.demo/SKILL.md",
                true,
                false,
                1L,
                "",
                Instant.now(),
                Instant.now()));

        AiExpertDefinition data = expertManagementService.find("data").orElseThrow();
        expertManagementService.save(new AiExpertDefinition(
                data.expertId(),
                data.name(),
                data.category(),
                data.description(),
                data.type(),
                data.modelProviderCode(),
                data.modelName(),
                data.systemPrompt(),
                data.toolIds(),
                java.util.List.of("skill.data.metrics", "skill.custom.demo"),
                data.options(),
                data.enabled(),
                data.builtIn(),
                data.version(),
                data.createdAt(),
                data.updatedAt()));

        ExpertRuntime runtime = adapter.buildRuntime(expertManagementService.find("data").orElseThrow());

        FileSystemSkillRepository repository = new FileSystemSkillRepository(tempDir);
        assertThat(repository.skillExists("skill.custom.demo")).isTrue();
        assertThat(repository.getSkill("skill.custom.demo")).isNotNull();
        assertThat(runtime.expertId()).isEqualTo("data");
    }

    @Test
    void warnsWhenFrontmatterNameDoesNotMatchSkillId() throws Exception {
        String markdown = """
                ---
                name: skill.other.name
                description: Mismatched skill
                ---
                Content
                """;
        Path source = tempDir.resolve("mismatch.md");
        Files.writeString(source, markdown);

        skillManagementService.save(new AiSkillDefinition(
                "skill.custom.mismatch",
                "Mismatch",
                "测试分类",
                "Mismatch skill",
                "file:" + source.toAbsolutePath(),
                "./data/skills/skill.custom.mismatch/SKILL.md",
                true,
                false,
                1L,
                "",
                Instant.now(),
                Instant.now()));

        skillFileSyncService.syncSkill(skillManagementService.find("skill.custom.mismatch").orElseThrow());

        FileSystemSkillRepository repository = new FileSystemSkillRepository(tempDir);
        assertThat(Files.exists(tempDir.resolve("skill.custom.mismatch/SKILL.md"))).isTrue();
        assertThat(repository.skillExists("skill.custom.mismatch")).isFalse();
        assertThat(repository.skillExists("skill.other.name")).isTrue();
    }
}
