package com.databuff.apm.web.ai;

public record CreateLlmProviderRequest(
        String providerCode,
        String displayName,
        String baseUrl,
        String defaultModel,
        String apiKey,
        Boolean enabled) {
}
