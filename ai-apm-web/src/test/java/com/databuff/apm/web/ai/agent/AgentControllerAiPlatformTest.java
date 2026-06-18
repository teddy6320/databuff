package com.databuff.apm.web.ai.agent;

import com.databuff.apm.web.ai.TestAiSupport;
import com.databuff.apm.web.ai.tool.ApmToolkit;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentControllerAiPlatformTest {

    @Test
    void defaultExpertIsBrain() {
        ApmToolkit toolkit = mock(ApmToolkit.class);
        when(toolkit.countRecentSpans(anyLong())).thenReturn(7);
        AgentBrainService brain = TestAiSupport.aiFixture().agentBrain(toolkit, new AiSessionStore());

        AgentBrainService.ChatResponse response = brain.chat(
                new AgentBrainService.ChatRequest(null, "最近 trace 有多少"));

        assertThat(response.expertId()).isEqualTo("brain");
        assertThat(response.reply()).contains("7");
    }

    @Test
    void routesToDataExpertWithoutAgentscope() {
        AgentBrainService brain = TestAiSupport.aiFixture().agentBrain(mock(ApmToolkit.class), new AiSessionStore());

        AgentBrainService.ChatResponse response = brain.chat(
                new AgentBrainService.ChatRequest(null, "data", "hello", false, Map.of(), null));

        assertThat(response.expertId()).isEqualTo("data");
        assertThat(response.reply()).contains("智能问数").contains("需要配置大模型");
    }

    @Test
    void returns404ForUnknownExpert() {
        AgentController controller = TestAiSupport.aiFixture().agentController(
                TestAiSupport.aiFixture().agentBrain(mock(ApmToolkit.class), new AiSessionStore()));

        ResponseEntity<Map<String, Object>> response = controller.handlePlatformChatException(
                AiPlatformChatException.expertNotFound("missing"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "expert_not_found");
    }
}
