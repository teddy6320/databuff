package com.databuff.apm.web.ai.mcp;

import com.databuff.apm.web.ai.agent.AgentBrainService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
public class McpController {

    private final McpServerService mcpServerService;
    private final AgentBrainService agentBrainService;

    public McpController(McpServerService mcpServerService, AgentBrainService agentBrainService) {
        this.mcpServerService = mcpServerService;
        this.agentBrainService = agentBrainService;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter mcpSession(@RequestBody(required = false) McpServerService.McpSessionRequest request)
            throws IOException {
        return mcpServerService.openSession(request, agentBrainService);
    }

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseHandshake() throws IOException {
        return mcpServerService.openSession(null, agentBrainService);
    }

    @GetMapping("/tools")
    public List<McpServerService.ToolDescriptor> tools() {
        return mcpServerService.tools();
    }

    @PostMapping("/chat")
    public AgentBrainService.ChatResponse chat(@RequestBody AgentBrainService.ChatRequest request) {
        return agentBrainService.chat(request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
