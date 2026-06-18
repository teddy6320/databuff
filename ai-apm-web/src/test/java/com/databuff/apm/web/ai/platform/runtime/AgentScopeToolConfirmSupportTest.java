package com.databuff.apm.web.ai.platform.runtime;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.state.AgentState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentScopeToolConfirmSupportTest {

    @Test
    void attachesAutoConfirmResultsForAskingToolCalls() {
        ToolUseBlock askingCall = ToolUseBlock.builder()
                .id("tool-1")
                .name("remoteMetricQuery")
                .input(Map.of("service", "demo-order"))
                .state(ToolCallState.ASKING)
                .build();
        Msg assistant = Msg.builder()
                .role(MsgRole.ASSISTANT)
                .content(askingCall)
                .build();
        AgentState agentState = AgentState.builder()
                .context(List.of(assistant))
                .build();

        ReActAgent agent = mock(ReActAgent.class);
        when(agent.getAgentState()).thenReturn(agentState);

        Msg userMessage = Msg.builder()
                .role(MsgRole.USER)
                .textContent("继续查询")
                .metadata(Map.of("sessionId", "session-1"))
                .build();

        Msg enriched = AgentScopeToolConfirmSupport.attachAutoConfirmIfNeeded(agent, userMessage);

        assertThat(enriched.getMetadata()).containsKey(Msg.METADATA_CONFIRM_RESULTS);
        Object raw = enriched.getMetadata().get(Msg.METADATA_CONFIRM_RESULTS);
        assertThat(raw).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<ConfirmResult> confirms = (List<ConfirmResult>) raw;
        assertThat(confirms).hasSize(1);
        assertThat(confirms.get(0).isConfirmed()).isTrue();
        assertThat(confirms.get(0).getToolCall().getId()).isEqualTo("tool-1");
        assertThat(enriched.getMetadata()).containsEntry("sessionId", "session-1");
    }

    @Test
    void leavesMessageUntouchedWhenNoAskingToolCalls() {
        ReActAgent agent = mock(ReActAgent.class);
        when(agent.getAgentState()).thenReturn(AgentState.builder().build());

        Msg userMessage = Msg.builder()
                .role(MsgRole.USER)
                .textContent("查 demo-order 错误率")
                .build();

        assertThat(AgentScopeToolConfirmSupport.attachAutoConfirmIfNeeded(agent, userMessage))
                .isSameAs(userMessage);
    }
}
