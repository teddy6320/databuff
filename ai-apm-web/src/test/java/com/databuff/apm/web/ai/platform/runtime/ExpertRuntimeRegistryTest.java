package com.databuff.apm.web.ai.platform.runtime;

import com.databuff.apm.web.ai.TestAiSupport;
import com.databuff.apm.web.ai.TestBeanSupport;
import com.databuff.apm.web.ai.InMemoryLlmProviderStore;
import com.databuff.apm.web.ai.UpdateLlmProviderRequest;
import com.databuff.apm.web.ai.platform.expert.AiExpertDefinition;
import com.databuff.apm.web.ai.platform.expert.BrainRoutingCatalog;
import com.databuff.apm.web.ai.platform.expert.ExpertManagementService;
import com.databuff.apm.web.ai.platform.skill.AiSkillDefinition;
import com.databuff.apm.web.ai.platform.skill.SkillManagementService;
import com.databuff.apm.web.ai.platform.tool.AiToolDefinition;
import com.databuff.apm.web.ai.platform.tool.ToolManagementService;
import com.databuff.apm.web.ai.platform.tool.ExpertToolResolver;
import com.databuff.apm.web.ai.platform.tool.ToolType;
import com.databuff.apm.web.ai.tool.ApmToolkit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.file.Path;
import java.time.Instant;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ExpertRuntimeRegistryTest {

    @TempDir
    Path tempDir;

    private ExpertRuntimeRegistry registry;
    private ExpertManagementService expertManagementService;
    private ToolManagementService toolManagementService;
    private SkillManagementService skillManagementService;
    private InMemoryLlmProviderStore providerStore;
    private ExpertToolResolver expertToolResolver;

    @BeforeEach
    void setUp() {
        TestAiSupport.AiFixture aiFixture = TestAiSupport.aiFixture();
        aiFixture.agentRuntimeConfig().setCustomSkillsDir(tempDir.toString());
        providerStore = aiFixture.store();
        providerStore.updateProvider("openai", new UpdateLlmProviderRequest(null, "sk-test", null, true));

        TestAiSupport.PlatformRuntimeFixture fixture =
                aiFixture.buildPlatformRuntime(mock(ApmToolkit.class));
        toolManagementService = TestBeanSupport.toolManagementService();
        skillManagementService = TestBeanSupport.skillManagementService();
        expertManagementService = TestBeanSupport.expertManagementService(toolManagementService, skillManagementService);

        expertToolResolver = new ExpertToolResolver(toolManagementService);
        BrainRoutingCatalog brainRoutingCatalog = new BrainRoutingCatalog(expertManagementService);
        SessionExpertRuntimeRegistry sessionExpertRuntimeRegistry = new SessionExpertRuntimeRegistry(
                skillManagementService,
                providerStore,
                expertToolResolver,
                fixture.runtimeAdapter(),
                brainRoutingCatalog);
        registry = new ExpertRuntimeRegistry(
                expertManagementService,
                toolManagementService,
                skillManagementService,
                providerStore,
                expertToolResolver,
                fixture.runtimeAdapter(),
                brainRoutingCatalog,
                sessionRegistryProvider(sessionExpertRuntimeRegistry));
        ObjectProvider<ExpertRuntimeRegistry> registryProvider = providerOf(registry);
        expertManagementService = TestBeanSupport.expertManagementService(
                toolManagementService, skillManagementService, null, registryProvider);
        toolManagementService = TestBeanSupport.toolManagementService(null, registryProvider);
        skillManagementService = TestBeanSupport.skillManagementService(null, registryProvider, null);
        providerStore = TestBeanSupport.llmProviderStore(registryProvider);
        providerStore.updateProvider("openai", new UpdateLlmProviderRequest(null, "sk-test", null, true));
        expertToolResolver = new ExpertToolResolver(toolManagementService);
        BrainRoutingCatalog refreshedCatalog = new BrainRoutingCatalog(expertManagementService);
        SessionExpertRuntimeRegistry refreshedSessionRegistry = new SessionExpertRuntimeRegistry(
                skillManagementService,
                providerStore,
                expertToolResolver,
                fixture.runtimeAdapter(),
                refreshedCatalog);
        registry = new ExpertRuntimeRegistry(
                expertManagementService,
                toolManagementService,
                skillManagementService,
                providerStore,
                expertToolResolver,
                fixture.runtimeAdapter(),
                refreshedCatalog,
                sessionRegistryProvider(refreshedSessionRegistry));
    }

    private static ObjectProvider<SessionExpertRuntimeRegistry> sessionRegistryProvider(
            SessionExpertRuntimeRegistry registry) {
        return new ObjectProvider<>() {
            @Override
            public SessionExpertRuntimeRegistry getObject() {
                return registry;
            }

            @Override
            public SessionExpertRuntimeRegistry getObject(Object... args) {
                return registry;
            }

            @Override
            public SessionExpertRuntimeRegistry getIfAvailable() {
                return registry;
            }

            @Override
            public SessionExpertRuntimeRegistry getIfUnique() {
                return registry;
            }

            @Override
            public void ifAvailable(Consumer<SessionExpertRuntimeRegistry> consumer) {
                consumer.accept(registry);
            }
        };
    }

    @Test
    void getOrCreateBuildsAndCachesRuntime() {
        ExpertRuntime first = registry.getOrCreate("data");
        ExpertRuntime second = registry.getOrCreate("data");

        assertThat(first).isSameAs(second);
        assertThat(registry.listStatus()).anyMatch(status ->
                status.expertId().equals("data") && status.loaded());
    }

    @Test
    void invalidatesWhenExpertSaved() {
        ExpertRuntime first = registry.getOrCreate("data");
        AiExpertDefinition updated = expertManagementService.find("data").orElseThrow();
        expertManagementService.save(new AiExpertDefinition(
                updated.expertId(),
                updated.name(),
                updated.category(),
                updated.description(),
                updated.type(),
                updated.modelProviderCode(),
                updated.modelName(),
                "Updated prompt for data expert.",
                updated.toolIds(),
                updated.skillIds(),
                updated.options(),
                updated.enabled(),
                updated.builtIn(),
                updated.version(),
                updated.createdAt(),
                updated.updatedAt()));
        ExpertRuntime second = registry.getOrCreate("data");

        assertThat(second).isNotSameAs(first);
        assertThat(((AgentScopeExpertRuntime) second).agent().getSysPrompt())
                .contains("Updated prompt");
    }

    @Test
    void invalidatesWhenSkillChanges() {
        ExpertRuntime first = registry.getOrCreate("data");
        AiSkillDefinition skill = skillManagementService.find("skill.data.metrics").orElseThrow();
        skillManagementService.save(new AiSkillDefinition(
                skill.skillId(),
                skill.name(),
                skill.category(),
                skill.description(),
                skill.contentUri(),
                skill.filePath(),
                skill.enabled(),
                skill.builtIn(),
                skill.version(),
                "checksum-v2",
                skill.createdAt(),
                skill.updatedAt()));
        ExpertRuntime second = registry.getOrCreate("data");

        assertThat(second).isNotSameAs(first);
    }

    @Test
    void invalidatesWhenProviderChanges() {
        ExpertRuntime first = registry.getOrCreate("data");
        providerStore.updateProvider("openai", new UpdateLlmProviderRequest(
                null, null, "gpt-4o", true));
        ExpertRuntime second = registry.getOrCreate("data");

        assertThat(second).isNotSameAs(first);
    }

    @Test
    void rejectsDisabledExpert() {
        AiExpertDefinition data = expertManagementService.find("data").orElseThrow();
        expertManagementService.save(new AiExpertDefinition(
                data.expertId(), data.name(), data.category(), data.description(), data.type(),
                data.modelProviderCode(), data.modelName(), data.systemPrompt(),
                data.toolIds(), data.skillIds(), data.options(),
                false, data.builtIn(), data.version(), data.createdAt(), data.updatedAt()));

        assertThatThrownBy(() -> registry.getOrCreate("data"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void invalidateByToolEvictsReferencingExperts() {
        ExpertRuntime first = registry.getOrCreate("data");
        Instant now = Instant.now();
        toolManagementService.save(new AiToolDefinition(
                "data.queryServicesAll",
                "服务列表查询",
                null,
                "desc",
                ToolType.JAVA_BEAN,
                "dataTools.queryServicesAll",
                "{}",
                "{}",
                true,
                true,
                1L,
                now,
                now));
        ExpertRuntime second = registry.getOrCreate("data");

        assertThat(second).isNotSameAs(first);
    }

    private static ObjectProvider<ExpertRuntimeRegistry> providerOf(ExpertRuntimeRegistry registry) {
        return new ObjectProvider<>() {
            @Override
            public ExpertRuntimeRegistry getObject() {
                return registry;
            }

            @Override
            public ExpertRuntimeRegistry getObject(Object... args) {
                return registry;
            }

            @Override
            public ExpertRuntimeRegistry getIfAvailable() {
                return registry;
            }

            @Override
            public ExpertRuntimeRegistry getIfUnique() {
                return registry;
            }

            @Override
            public void ifAvailable(Consumer<ExpertRuntimeRegistry> consumer) {
                consumer.accept(registry);
            }
        };
    }
}
