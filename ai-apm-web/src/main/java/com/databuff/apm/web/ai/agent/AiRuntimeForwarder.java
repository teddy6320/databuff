package com.databuff.apm.web.ai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forwards chat to the cluster owner node over HTTP when the local instance is not the owner.
 */
@Component
public class AiRuntimeForwarder {

    private static final Logger log = LoggerFactory.getLogger(AiRuntimeForwarder.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AiRuntimeRouter runtimeRouter;
    private final HttpClient httpClient;
    private final Duration deadline;

    public AiRuntimeForwarder(
            AiRuntimeRouter runtimeRouter,
            @Value("${apm.ai.cluster.forward-timeout-seconds:120}") long forwardTimeoutSeconds) {
        this.runtimeRouter = runtimeRouter;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.deadline = Duration.ofSeconds(Math.max(5, forwardTimeoutSeconds));
    }

    public AgentBrainService.ChatResponse forwardChat(
            AiRuntimeRouter.RouteDecision route,
            AgentBrainService.ChatRequest request) {
        String endpoint = runtimeRouter.endpoint(route.ownerNodeId())
                .orElseThrow(() -> new AiPlatformChatException(
                        "forward_required",
                        501,
                        "remote owner routing is not configured for node: " + route.ownerNodeId()));
        try {
            Map<String, Object> body = buildBody(request);
            String base = normalizeBaseUrl(endpoint);
            Map<String, Object> submit = postJson(base + "/webapi/api/v1/ai/chat/submit", body);
            String sessionId = stringValue(submit.get("sessionId"), request.sessionId());
            String expertId = stringValue(submit.get("expertId"), request.resolvedExpertId());
            long deadlineMs = System.currentTimeMillis() + deadline.toMillis();
            while (System.currentTimeMillis() < deadlineMs) {
                Map<String, Object> poll = getJson(base + "/webapi/api/v1/ai/sessions/"
                        + encodePathSegment(sessionId) + "/messages");
                boolean running = Boolean.TRUE.equals(poll.get("running"));
                if (!running) {
                    String reply = extractLatestAssistantReply(poll.get("messages"));
                    return new AgentBrainService.ChatResponse(sessionId, expertId, reply, true);
                }
                Thread.sleep(300L);
            }
            throw new AiPlatformChatException("forward_timeout", 504, "remote chat timed out on " + route.ownerNodeId());
        } catch (AiPlatformChatException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Forward chat to {} failed: {}", route.ownerNodeId(), e.getMessage());
            throw new AiPlatformChatException(
                    "forward_failed",
                    502,
                    "forward chat to owner " + route.ownerNodeId() + " failed: " + e.getMessage());
        }
    }

    private Map<String, Object> buildBody(AgentBrainService.ChatRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("sessionId", request.sessionId());
        body.put("expertId", request.expertId());
        body.put("message", request.message());
        body.put("stream", false);
        body.put("context", request.context());
        body.put("requestId", request.requestId());
        return body;
    }

    private Map<String, Object> postJson(String url, Map<String, Object> body) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(deadline)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new AiPlatformChatException(
                    "forward_failed",
                    response.statusCode(),
                    "remote chat failed (" + response.statusCode() + "): " + response.body());
        }
        return OBJECT_MAPPER.readValue(response.body(), MAP_TYPE);
    }

    private Map<String, Object> getJson(String url) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new AiPlatformChatException(
                    "forward_failed",
                    response.statusCode(),
                    "remote poll failed (" + response.statusCode() + "): " + response.body());
        }
        return OBJECT_MAPPER.readValue(response.body(), MAP_TYPE);
    }

    @SuppressWarnings("unchecked")
    private String extractLatestAssistantReply(Object messagesObj) {
        if (!(messagesObj instanceof List<?> messages)) {
            return "";
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            Object item = messages.get(i);
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            if ("assistant".equals(String.valueOf(map.get("role")))) {
                return stringValue(map.get("content"), "");
            }
        }
        return "";
    }

    private static String encodePathSegment(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String normalizeBaseUrl(String endpoint) {
        String trimmed = endpoint.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        }
        return "http://" + (trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed);
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? fallback : text;
    }
}
