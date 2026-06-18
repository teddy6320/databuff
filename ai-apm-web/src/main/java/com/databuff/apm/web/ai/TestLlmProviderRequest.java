package com.databuff.apm.web.ai;

public record TestLlmProviderRequest(
        String baseUrl,
        String apiKey,
        String apiType,
        String modelId,
        String providerCode) {

    public TestLlmProviderRequest(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, null, null, null);
    }
}
