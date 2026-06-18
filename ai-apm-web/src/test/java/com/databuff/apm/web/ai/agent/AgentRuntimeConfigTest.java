package com.databuff.apm.web.ai.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRuntimeConfigTest {

    @Test
    void exposesRuntimeSummary() {
        AgentRuntimeConfig config = new AgentRuntimeConfig();
        config.setAgentscopeEnabled(true);
        config.setBrainAgentName("brain");
        config.setDataAgentName("data");
        config.logReady();
        assertThat(config.isAgentscopeEnabled()).isTrue();
        assertThat(config.getBrainAgentName()).isEqualTo("brain");
        assertThat(config.getDataAgentName()).isEqualTo("data");
        AgentRuntimeConfig.RuntimeSummary summary = config.summary();
        assertThat(summary.delegate()).isEqualTo("AiChatOrchestrator");
        assertThat(summary.agentscopeEnabled()).isTrue();
        assertThat(summary.builtinSkillsDir()).contains("deploy/common/skills");
        assertThat(summary.customSkillsDir()).endsWith("skills");
    }

    @Test
    void resolvesSkillDirectories() throws Exception {
        java.nio.file.Path builtin = java.nio.file.Files.createTempDirectory("builtin-skills-");
        java.nio.file.Path custom = java.nio.file.Files.createTempDirectory("custom-skills-");
        AgentRuntimeConfig config = new AgentRuntimeConfig();
        config.setBuiltinSkillsDir(builtin.toString());
        config.setCustomSkillsDir(custom.toString());
        assertThat(config.builtinSkillsDirectory()).isEqualTo(builtin);
        assertThat(config.customSkillsDirectory()).isEqualTo(custom);
        assertThat(config.summary().builtinSkillsDir()).contains("builtin-skills");
        assertThat(config.summary().customSkillsDir()).contains("custom-skills");
    }

    @Test
    void resolvesBuiltinSkillsDirectoryFromFallbackWhenConfiguredPathMissing() {
        AgentRuntimeConfig config = new AgentRuntimeConfig();
        config.setBuiltinSkillsDir("/tmp/nonexistent-builtin-skills-" + System.nanoTime());
        assertThat(config.builtinSkillsDirectory().toString()).contains("deploy/common/skills");
        assertThat(java.nio.file.Files.isDirectory(config.builtinSkillsDirectory())).isTrue();
    }
}
