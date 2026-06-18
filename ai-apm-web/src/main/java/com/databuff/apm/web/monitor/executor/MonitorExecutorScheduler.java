package com.databuff.apm.web.monitor.executor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * In-process task executor for monitor rules: evaluate → raw event → silence → alert → response.
 */
@Component
public class MonitorExecutorScheduler {

    private final EventRuleExecutionOrchestrator orchestrator;

    public MonitorExecutorScheduler(EventRuleExecutionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(cron = "${apm.alarm.evaluation-cron:0 * * * * ?}")
    public void evaluateRules() {
        orchestrator.runAllMonitors();
    }
}
