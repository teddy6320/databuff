package com.databuff.apm.web.ai.mcp;

import com.databuff.apm.web.ai.TestAiSupport;
import com.databuff.apm.web.ai.agent.AiSessionStore;
import com.databuff.apm.web.ai.agent.AgentBrainService;
import com.databuff.apm.web.ai.tool.ApmToolkit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class McpControllerTest {

    @Test
    void exposesToolsAndChat() {
        TestAiSupport.AiFixture fixture = TestAiSupport.aiFixture();
        AgentBrainService brain = fixture.agentBrain(Mockito.mock(ApmToolkit.class), new AiSessionStore());
        McpController controller = new McpController(new McpServerService(), brain);
        assertThat(controller.tools()).hasSize(3);
        AgentBrainService.ChatResponse response = controller.chat(
                new AgentBrainService.ChatRequest(null, "help"));
        assertThat(response.reply()).contains("DataBuff APM 助手");
    }

    @Test
    void sseHandshakeAndHealth() throws Exception {
        McpController controller = new McpController(new McpServerService(), TestAiSupport.aiFixture()
                .agentBrain(mock(ApmToolkit.class), new AiSessionStore()));
        SseEmitter emitter = controller.sseHandshake();
        assertThat(emitter).isNotNull();
        assertThat(controller.health()).isEqualTo(Map.of("status", "ok"));
    }

    @Test
    void postMcpSessionAcceptsPrompt() throws Exception {
        McpController controller = new McpController(new McpServerService(), TestAiSupport.aiFixture()
                .agentBrain(mock(ApmToolkit.class), new AiSessionStore()));
        SseEmitter emitter = controller.mcpSession(new McpServerService.McpSessionRequest(null, "help"));
        assertThat(emitter).isNotNull();
    }
}
