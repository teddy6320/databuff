package com.databuff.apm.web.ai.mcp;

import com.databuff.apm.web.ai.TestAiSupport;
import com.databuff.apm.web.ai.agent.AiSessionStore;
import com.databuff.apm.web.ai.tool.ApmToolkit;
import com.databuff.apm.web.ai.agent.AgentBrainService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class McpServerServiceTest {

    @Test
    void exposesCapabilitiesAndTools() {
        McpServerService service = new McpServerService();
        assertThat(service.capabilities()).containsEntry("protocol", "databuff-apm-mcp");
        assertThat(service.tools()).extracting(McpServerService.ToolDescriptor::name)
                .containsExactly("query_error_rate", "query_trace_count", "chat");
    }

    @Test
    void openSessionStreamsEvents() throws Exception {
        McpServerService service = new McpServerService();
        AgentBrainService brain = TestAiSupport.aiFixture().agentBrain(
                org.mockito.Mockito.mock(ApmToolkit.class),
                new AiSessionStore());
        assertThat(service.openSession(new McpServerService.McpSessionRequest(null, "help"), brain)).isNotNull();
    }
}
