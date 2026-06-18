package com.databuff.apm.web.ai;

public record UpdateLlmProviderRequest(
        String baseUrl,
        String apiKey,
        String defaultModel,
        Boolean enabled) {
}
