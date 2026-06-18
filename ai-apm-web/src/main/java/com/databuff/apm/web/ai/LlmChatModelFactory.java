package com.databuff.apm.web.ai;

import io.agentscope.core.model.AnthropicChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;

public final class LlmChatModelFactory {

    private LlmChatModelFactory() {
    }

    public static Model build(
            OpenAiCompatibleChatClient.ResolvedLlmProvider provider,
            String modelName,
            boolean stream) {
        String resolvedModel = modelName == null || modelName.isBlank()
                ? provider.defaultModel()
                : modelName;
        String apiKey = provider.apiKey() == null ? "" : provider.apiKey();
        String baseUrl = normalizeBaseUrl(provider.baseUrl());
        if (LlmApiTypes.isAnthropic(provider.apiType())) {
            return AnthropicChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(resolvedModel)
                    .baseUrl(baseUrl)
                    .stream(stream)
                    .build();
        }
        return OpenAIChatModel.builder()
                .apiKey(apiKey)
                .modelName(resolvedModel)
                .baseUrl(baseUrl)
                .stream(stream)
                .build();
    }

    public static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api.openai.com/v1";
        }
        return baseUrl.trim().replaceAll("/$", "");
    }
}
