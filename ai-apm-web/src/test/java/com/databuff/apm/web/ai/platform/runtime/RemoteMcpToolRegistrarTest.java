package com.databuff.apm.web.ai.platform.runtime;

import com.databuff.apm.web.ai.platform.tool.AiToolDefinition;
import com.databuff.apm.web.ai.platform.tool.ToolType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteMcpToolRegistrarTest {

    private RemoteMcpToolRegistrar registrar;

    @BeforeEach
    void setUp() {
        registrar = new RemoteMcpToolRegistrar(new ObjectMapper());
    }

    @Test
    void parsesEndpointTransportAndHeadersFromConfigJson() {
        RemoteMcpToolRegistrar.McpConnectionConfig config = registrar.parseConfig(mcpTool(
                "remote.metrics",
                "https://ignored.example/mcp",
                """
                {
                  "endpoint": "https://mcp.example/sse",
                  "transport": "Streamable HTTP",
                  "headers": {"Authorization": "Bearer token"}
                }
                """));

        assertThat(config.endpoint()).isEqualTo("https://mcp.example/sse");
        assertThat(config.transport()).isEqualTo("STREAMABLE_HTTP");
        assertThat(config.headers()).containsEntry("Authorization", "Bearer token");
    }

    @Test
    void fallsBackToImplementationWhenConfigEndpointMissing() {
        RemoteMcpToolRegistrar.McpConnectionConfig config = registrar.parseConfig(mcpTool(
                "remote.metrics",
                "https://mcp.example/sse",
                "{}"));

        assertThat(config.endpoint()).isEqualTo("https://mcp.example/sse");
        assertThat(config.transport()).isEqualTo("SSE");
    }

    @Test
    void normalizesTransportAliases() {
        assertThat(RemoteMcpToolRegistrar.normalizeTransport("sse")).isEqualTo("SSE");
        assertThat(RemoteMcpToolRegistrar.normalizeTransport("streamable-http")).isEqualTo("STREAMABLE_HTTP");
        assertThat(RemoteMcpToolRegistrar.normalizeTransport("HTTP")).isEqualTo("STREAMABLE_HTTP");
    }

    private static AiToolDefinition mcpTool(String toolId, String implementation, String configJson) {
        Instant now = Instant.now();
        return new AiToolDefinition(
                toolId,
                toolId,
                "远程 MCP",
                "desc",
                ToolType.MCP,
                implementation,
                "{}",
                configJson,
                true,
                false,
                1L,
                now,
                now);
    }
}
