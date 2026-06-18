package com.databuff.apm.web.ai;

public final class LlmApiTypes {

    public static final String OPENAI_COMPLETIONS = "openai-completions";
    public static final String ANTHROPIC_MESSAGES = "anthropic-messages";

    private LlmApiTypes() {
    }

    public static boolean isAnthropic(String apiType) {
        return ANTHROPIC_MESSAGES.equals(normalize(apiType));
    }

    public static String normalize(String apiType) {
        if (apiType == null || apiType.isBlank()) {
            return OPENAI_COMPLETIONS;
        }
        return apiType.trim();
    }
}
