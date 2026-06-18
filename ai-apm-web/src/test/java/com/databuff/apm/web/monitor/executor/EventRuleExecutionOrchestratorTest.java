package com.databuff.apm.web.monitor.executor;

import com.databuff.apm.common.cluster.coordination.ClusterInstanceCoordinator;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.EventRuleStore;
import com.databuff.apm.web.monitor.InMemoryEventRuleStore;
import com.databuff.apm.web.monitor.pipeline.EventRulePipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventRuleExecutionOrchestratorTest {

    private EventRuleStore ruleStore;
    private EventRulePipeline pipeline;
    private ClusterInstanceCoordinator coordinator;

    @BeforeEach
    void setUp() {
        ruleStore = new InMemoryEventRuleStore();
        pipeline = mock(EventRulePipeline.class);
        coordinator = mock(ClusterInstanceCoordinator.class);
        when(pipeline.filterEnabledRules(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void evaluatesOnlyOwnedShardInClusterMode() {
        ruleStore.save(sampleRule(1L, "checkout"));
        ruleStore.save(sampleRule(2L, "billing"));

        when(coordinator.effectiveClusterEnabled()).thenReturn(true);
        when(coordinator.filterOwned(anyList(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> List.of(((List<EventRule>) invocation.getArgument(0)).get(0)));

        EventRuleExecutionOrchestrator orchestrator = new EventRuleExecutionOrchestrator(
                ruleStore, pipeline, new MonitorTaskPool(1, 1, 10), coordinator);

        orchestrator.runAllMonitors();
        org.mockito.Mockito.verify(coordinator).filterOwned(anyList(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void evaluatesAllRulesWhenClusterDisabled() {
        EventRule rule1 = sampleRule(1L, "checkout");
        ruleStore.save(rule1);
        when(coordinator.effectiveClusterEnabled()).thenReturn(false);

        MonitorTaskPool pool = new MonitorTaskPool(1, 1, 10);
        EventRuleExecutionOrchestrator orchestrator = new EventRuleExecutionOrchestrator(
                ruleStore, pipeline, pool, coordinator);

        orchestrator.runAllMonitors();
        org.mockito.Mockito.verify(coordinator, org.mockito.Mockito.never())
                .filterOwned(anyList(), org.mockito.ArgumentMatchers.any());
    }

    private static EventRule sampleRule(long id, String service) {
        return new EventRule(
                id,
                "rule-" + id,
                EventRule.CLASSIFY_SINGLE,
                EventRule.WAY_THRESHOLD,
                service,
                EventRule.METRIC_ERROR_RATE,
                0.1,
                EventRule.COMPARATOR_GT,
                true,
                null,
                null);
    }
}
