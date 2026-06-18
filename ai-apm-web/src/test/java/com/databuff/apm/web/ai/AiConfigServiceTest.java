package com.databuff.apm.web.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiConfigServiceTest {

    @Test
    void exposesProviderListAndStatus() {
        AiConfigService service = TestAiSupport.configService();
        assertThat(service.listProviders()).isNotEmpty();
        assertThat(service.aiReady()).isFalse();
        service.updateProvider("deepseek", new UpdateLlmProviderRequest(null, "sk-test", null, true));
        assertThat(service.aiReady()).isTrue();
        assertThat(service.testProvider(new TestLlmProviderRequest("", null)).ok()).isFalse();
    }
}
