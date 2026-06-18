package com.databuff.apm.web.ai.agent;

import com.databuff.apm.web.auth.JwtTokenService;
import com.databuff.apm.web.auth.RequestAuthSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ChatRequestContextResolver {

    private final JwtTokenService jwtTokenService;

    public ChatRequestContextResolver(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public AgentBrainService.ChatRequest enrich(HttpServletRequest request, AgentBrainService.ChatRequest body) {
        if (body == null) {
            return null;
        }
        String userName = RequestAuthSupport.resolveUserName(request, body.userName(), jwtTokenService);
        return new AgentBrainService.ChatRequest(
                body.sessionId(),
                body.expertId(),
                body.message(),
                body.stream(),
                body.context(),
                body.requestId(),
                body.modelProviderCode(),
                body.modelName(),
                userName);
    }
}
