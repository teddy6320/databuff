package com.databuff.apm.web.ai;

import java.util.List;

public record SaveLlmProviderRequest(
        String providerCode,
        String providerName,
        String apiType,
        String baseUrl,
        String apiKey,
        Boolean enabled,
        Boolean defaultProvider,
        String defaultModelId,
        List<LlmModelView> models) {
}
