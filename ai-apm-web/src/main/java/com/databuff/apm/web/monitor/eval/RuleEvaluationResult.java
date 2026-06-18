package com.databuff.apm.web.monitor.eval;

public record RuleEvaluationResult(
        boolean triggered,
        String level,
        String message,
        String detectionWay,
        String service,
        String groupKey) {

    public static RuleEvaluationResult normal() {
        return new RuleEvaluationResult(false, "warning", "", "", "*", "default");
    }
}
