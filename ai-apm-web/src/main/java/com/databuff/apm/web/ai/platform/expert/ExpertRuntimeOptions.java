package com.databuff.apm.web.ai.platform.expert;

public record ExpertRuntimeOptions(
        String category,
        int maxIters,
        boolean stream,
        boolean enablePlan,
        boolean dynamicSkillsEnabled,
        int timeoutSeconds,
        int maxConcurrentSubtasks,
        boolean exposeToolEvents,
        ExpertToolAccessMode toolAccessMode) {

    public ExpertRuntimeOptions {
        if (category == null || category.isBlank()) {
            category = "默认分类";
        }
        if (toolAccessMode == null) {
            toolAccessMode = ExpertToolAccessMode.ALLOWLIST;
        }
    }

    public static ExpertRuntimeOptions defaults() {
        return new ExpertRuntimeOptions("默认分类", 8, false, false, false, 120, 3, true, ExpertToolAccessMode.ALLOWLIST);
    }
}
