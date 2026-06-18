package com.databuff.apm.web.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventRuleServiceTest {

    private EventRuleService service;

    @BeforeEach
    void setUp() {
        service = new EventRuleService(new InMemoryEventRuleStore());
    }

    @Test
    void listsRulesStartsEmpty() {
        assertThat(service.listRules()).isEmpty();
    }

    @Test
    void createsMutationRule() {
        EventRule created = service.createRule(new EventRuleStore.CreateRequest(
                "checkout mutation", "checkout", 0.3, "gt", true, EventRule.WAY_MUTATION));
        assertThat(created.detectionWay()).isEqualTo(EventRule.WAY_MUTATION);
    }

    @Test
    void createsThresholdRule() {
        EventRule created = service.createRule(new EventRuleStore.CreateRequest(
                "checkout errors", "checkout", 0.1, "gt", true));
        assertThat(created.ruleName()).isEqualTo("checkout errors");
        assertThat(created.detectionWay()).isEqualTo(EventRule.WAY_THRESHOLD);
    }

    @Test
    void rejectsInvalidThreshold() {
        assertThatThrownBy(() -> service.createRule(new EventRuleStore.CreateRequest(
                "bad", "svc", 1.5, "gt", true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void togglesRuleEnabled() {
        EventRule rule = service.createRule(new EventRuleStore.CreateRequest(
                "checkout errors", "checkout", 0.1, "gt", true));
        assertThat(service.updateEnabled(rule.id(), false)).isPresent();
    }
}
