package com.databuff.apm.web.ai.platform.expert;

import com.databuff.apm.web.ai.TestBeanSupport;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrainRoutingCatalogTest {

    @Test
    void includesEnabledSpecialistsAndExcludesBrain() {
        ExpertManagementService service = service();
        BrainRoutingCatalog catalog = new BrainRoutingCatalog(service);

        assertThat(catalog.listRoutableExperts()).extracting(AiExpertDefinition::expertId)
                .contains("data", "inspection")
                .doesNotContain("brain");
    }

    @Test
    void brainPromptListsNewExpertPurposeAfterSave() {
        ExpertManagementService service = service();
        BrainRoutingCatalog catalog = new BrainRoutingCatalog(service);
        Instant now = Instant.now();
        service.save(new AiExpertDefinition(
                "capacity", "容量规划", "容量", "分析服务容量与扩缩容建议", ExpertType.CUSTOM,
                null, null, "capacity prompt", List.of("data.queryServicesAll"),
                List.of("skill.data.metrics"), ExpertRuntimeOptions.defaults(), true, false, 0, now, now));

        String prompt = catalog.resolveBrainSystemPrompt(service.find("brain").orElseThrow());

        assertThat(prompt)
                .contains("可派发的数字专家")
                .contains("`capacity`")
                .contains("分析服务容量与扩缩容建议")
                .doesNotContain("Routing rules:")
                .doesNotContain("-> dispatch to `data`");
    }

    @Test
    void fingerprintChangesWhenRoutableExpertChanges() {
        ExpertManagementService service = service();
        BrainRoutingCatalog catalog = new BrainRoutingCatalog(service);
        String before = catalog.routableExpertsFingerprint();
        Instant now = Instant.now();
        service.save(new AiExpertDefinition(
                "oncall", "值班助手", "值班", "处理告警升级与值班交接", ExpertType.CUSTOM,
                null, null, "oncall prompt", List.of("data.queryServicesAll"),
                List.of("skill.data.metrics"), ExpertRuntimeOptions.defaults(), true, false, 0, now, now));

        assertThat(catalog.routableExpertsFingerprint()).isNotEqualTo(before);
    }

    private static ExpertManagementService service() {
        return TestBeanSupport.expertManagementService(
                TestBeanSupport.toolManagementService(),
                TestBeanSupport.skillManagementService());
    }
}
