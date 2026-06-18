package com.databuff.apm.web.ai;

import io.agentscope.core.model.AnthropicChatModel;
import io.agentscope.core.model.OpenAIChatModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmChatModelFactoryTest {

    @Test
    void buildsOpenAiModelByDefault() {
        var provider = new OpenAiCompatibleChatClient.ResolvedLlmProvider(
                "openai", "https://api.openai.com/v1", "gpt-4o-mini", "sk-test", LlmApiTypes.OPENAI_COMPLETIONS);
        assertThat(LlmChatModelFactory.build(provider, "gpt-4o-mini", false))
                .isInstanceOf(OpenAIChatModel.class);
    }

    @Test
    void buildsAnthropicModelForAnthropicApiType() {
        var provider = new OpenAiCompatibleChatClient.ResolvedLlmProvider(
                "minimax",
                "https://api.minimaxi.com/anthropic",
                "MiniMax-M3",
                "sk-test",
                LlmApiTypes.ANTHROPIC_MESSAGES);
        assertThat(LlmChatModelFactory.build(provider, "MiniMax-M3", false))
                .isInstanceOf(AnthropicChatModel.class);
    }
}
