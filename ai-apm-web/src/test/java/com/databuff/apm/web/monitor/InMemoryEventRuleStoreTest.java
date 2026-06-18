package com.databuff.apm.web.monitor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryEventRuleStoreTest {

    @Test
    void updatesExistingRule() {
        InMemoryEventRuleStore store = new InMemoryEventRuleStore();
        EventRule created = store.create(new EventRuleStore.CreateRequest(
                "checkout errors", "checkout", 0.1, "gt", true));
        EventRule updated = store.save(created.withEnabled(false));
        assertThat(updated.enabled()).isFalse();
        assertThat(store.findById(created.id())).get().isEqualTo(updated);
    }

    @Test
    void createsMutationRuleAndReplacesAll() {
        InMemoryEventRuleStore store = new InMemoryEventRuleStore();
        EventRule created = store.create(new EventRuleStore.CreateRequest(
                "mutation", "checkout", 0.3, "gt", true, EventRule.WAY_MUTATION));
        assertThat(created.detectionWay()).isEqualTo(EventRule.WAY_MUTATION);

        store.replaceAll(List.of(created));
        store.syncIdSequence(10);
        assertThat(store.list()).hasSize(1);
        assertThat(store.delete(created.id())).isTrue();
    }
}
