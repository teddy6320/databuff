package com.databuff.apm.web.monitor.executor;

import com.databuff.apm.common.cluster.coordination.ClusterInstanceCoordinator;
import com.databuff.apm.web.monitor.pipeline.EventRulePipeline;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.EventRuleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventRuleExecutionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(EventRuleExecutionOrchestrator.class);

    private final EventRuleStore ruleStore;
    private final EventRulePipeline monitorPipeline;
    private final MonitorTaskPool monitorTaskPool;
    private final ClusterInstanceCoordinator clusterCoordinator;

    public EventRuleExecutionOrchestrator(
            EventRuleStore ruleStore,
            EventRulePipeline monitorPipeline,
            MonitorTaskPool monitorTaskPool,
            ClusterInstanceCoordinator clusterCoordinator) {
        this.ruleStore = ruleStore;
        this.monitorPipeline = monitorPipeline;
        this.monitorTaskPool = monitorTaskPool;
        this.clusterCoordinator = clusterCoordinator;
    }

    public void runAllMonitors() {
        long started = System.currentTimeMillis();
        List<EventRule> rules = shardOwnedRules(monitorPipeline.filterEnabledRules(ruleStore.list()));
        if (rules.isEmpty()) {
            return;
        }
        monitorTaskPool.runAll(rules.stream()
                .map(rule -> (Runnable) () -> {
                    try {
                        monitorPipeline.evaluateRule(rule);
                    } catch (Exception e) {
                        log.warn("monitor rule {} failed: {}", rule.id(), e.toString());
                    }
                })
                .toList());
        log.debug("evaluated {} monitor rules on node {} in {} ms",
                rules.size(), clusterCoordinator.localNodeId(), System.currentTimeMillis() - started);
    }

    private List<EventRule> shardOwnedRules(List<EventRule> rules) {
        if (!clusterCoordinator.effectiveClusterEnabled()) {
            return rules;
        }
        return clusterCoordinator.filterOwned(rules, rule -> String.valueOf(rule.id()));
    }
}
