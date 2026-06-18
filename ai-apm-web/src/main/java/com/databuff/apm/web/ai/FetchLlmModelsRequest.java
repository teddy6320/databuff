package com.databuff.apm.web.ai;

public record FetchLlmModelsRequest(
        String providerCode,
        String apiType,
        String baseUrl,
        String apiKey) {
}
