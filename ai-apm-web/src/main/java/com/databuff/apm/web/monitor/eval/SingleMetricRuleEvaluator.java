package com.databuff.apm.web.monitor.eval;

import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.RuleMetricEvaluationService;
import com.databuff.apm.web.monitor.RuleMetricEvaluationService.GroupMetricValue;
import com.databuff.apm.web.monitor.ThresholdAlarmCheck;
import com.databuff.apm.web.monitor.ThresholdEvaluationService;
import com.databuff.apm.web.portal.PortalTimeParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SingleMetricRuleEvaluator {

    private final ThresholdEvaluationService evaluationService;
    private final RuleMetricEvaluationService ruleMetricEvaluationService;
    private final ThresholdAlarmMessageFormatter messageFormatter;

    public SingleMetricRuleEvaluator(
            ThresholdEvaluationService evaluationService,
            RuleMetricEvaluationService ruleMetricEvaluationService,
            ThresholdAlarmMessageFormatter messageFormatter) {
        this.evaluationService = evaluationService;
        this.ruleMetricEvaluationService = ruleMetricEvaluationService;
        this.messageFormatter = messageFormatter;
    }

    public RuleEvaluationResult evaluate(EventRule rule, long lookbackMillis) {
        List<RuleEvaluationResult> results = evaluateAll(rule, lookbackMillis);
        return results.isEmpty() ? RuleEvaluationResult.normal() : results.get(0);
    }

    public List<RuleEvaluationResult> evaluateAll(EventRule rule, long lookbackMillis) {
        Map<String, Object> query = EventRulePayloadParser.parseQuery(rule.queryJson());
        Map<String, Object> primary = EventRulePayloadParser.primaryQueryItem(query);
        if (!primary.isEmpty()) {
            lookbackMillis = EventRulePayloadParser.lookbackMinutes(primary) * 60_000L;
        }
        String way = EventRulePayloadParser.normalizeWay(
                !primary.isEmpty()
                        ? String.valueOf(primary.getOrDefault("way", rule.detectionWay()))
                        : rule.detectionWay());
        boolean catalogRule = rule.queryJson() != null && !rule.queryJson().isBlank();
        if (catalogRule) {
            if (EventRule.WAY_MUTATION.equals(way)) {
                return evaluateCatalogMutationAll(rule, lookbackMillis);
            }
            return evaluateCatalogThresholdAll(rule, lookbackMillis);
        }
        RuleEvaluationResult single = evaluateLegacy(rule, lookbackMillis, way);
        if (!single.triggered()) {
            return List.of();
        }
        return List.of(single);
    }

    private RuleEvaluationResult evaluateLegacy(EventRule rule, long lookbackMillis, String way) {
        if (EventRule.WAY_MUTATION.equals(way)) {
            return evaluateMutation(rule, lookbackMillis);
        }
        return evaluateThreshold(rule, lookbackMillis);
    }

    private RuleEvaluationResult evaluateThreshold(EventRule rule, long lookbackMillis) {
        double rate = evaluationService.currentErrorRate(rule.service(), lookbackMillis);
        if (ThresholdAlarmCheck.breached(rate, rule.threshold(), rule.comparator())) {
            String message = messageFormatter.legacyErrorRateThresholdMessage(
                    rule.service(), rate, rule.threshold());
            return new RuleEvaluationResult(
                    true, "critical", message, EventRule.WAY_THRESHOLD, rule.service(), rule.service());
        }
        return RuleEvaluationResult.normal();
    }

    private RuleEvaluationResult evaluateMutation(EventRule rule, long lookbackMillis) {
        long to = PortalTimeParser.portalEndNow();
        long currentFrom = to - lookbackMillis;
        double current = evaluationService.errorRateBetween(rule.service(), currentFrom, to);
        double previous = evaluationService.errorRateBetween(
                rule.service(), to - lookbackMillis * 2, currentFrom);
        double delta = Math.abs(current - previous);
        if (ThresholdAlarmCheck.breached(delta, rule.threshold(), rule.comparator())) {
            String message = messageFormatter.legacyErrorRateMutationMessage(
                    rule.service(), delta, rule.threshold());
            return new RuleEvaluationResult(
                    true, "critical", message, EventRule.WAY_MUTATION, rule.service(), rule.service());
        }
        return RuleEvaluationResult.normal();
    }

    private List<RuleEvaluationResult> evaluateCatalogThresholdAll(EventRule rule, long lookbackMillis) {
        List<GroupMetricValue> groups = ruleMetricEvaluationService.evaluateRuleGroups(rule, lookbackMillis);
        List<RuleEvaluationResult> results = new ArrayList<>();
        for (GroupMetricValue group : groups) {
            if (ThresholdAlarmCheck.breached(group.value(), rule.threshold(), rule.comparator())) {
                String message = messageFormatter.thresholdMessage(
                        rule, group.value(), rule.threshold(), group.groupKey(), group.service());
                results.add(new RuleEvaluationResult(
                        true,
                        "critical",
                        message,
                        EventRule.WAY_THRESHOLD,
                        group.service(),
                        group.groupKey()));
            }
        }
        return results;
    }

    private List<RuleEvaluationResult> evaluateCatalogMutationAll(EventRule rule, long lookbackMillis) {
        long to = PortalTimeParser.portalEndNow();
        long currentFrom = to - lookbackMillis;
        List<GroupMetricValue> currentGroups = ruleMetricEvaluationService.evaluateRuleGroups(rule, currentFrom, to);
        List<GroupMetricValue> previousGroups = ruleMetricEvaluationService.evaluateRuleGroups(
                rule, to - lookbackMillis * 2, currentFrom);
        Map<String, GroupMetricValue> previousByGroup = previousGroups.stream()
                .collect(Collectors.toMap(GroupMetricValue::groupKey, Function.identity(), (left, right) -> left));
        List<RuleEvaluationResult> results = new ArrayList<>();
        for (GroupMetricValue current : currentGroups) {
            GroupMetricValue previous = previousByGroup.get(current.groupKey());
            double previousValue = previous == null ? 0 : previous.value();
            double delta = Math.abs(current.value() - previousValue);
            if (ThresholdAlarmCheck.breached(delta, rule.threshold(), rule.comparator())) {
                String message = messageFormatter.mutationMessage(
                        rule, delta, rule.threshold(), current.groupKey(), current.service());
                results.add(new RuleEvaluationResult(
                        true,
                        "critical",
                        message,
                        EventRule.WAY_MUTATION,
                        current.service(),
                        current.groupKey()));
            }
        }
        return results;
    }
}
