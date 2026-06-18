package com.databuff.apm.web.ai;

import java.util.List;

public record LlmModelView(
        String modelId,
        String displayName,
        Integer contextWindow,
        Integer maxOutputTokens,
        List<LlmEnvVarItem> envVars,
        boolean defaultModel) {
}
