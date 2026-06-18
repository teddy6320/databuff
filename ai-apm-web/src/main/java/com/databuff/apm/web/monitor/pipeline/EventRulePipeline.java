package com.databuff.apm.web.monitor.pipeline;

import com.databuff.apm.web.monitor.eval.SingleMetricRuleEvaluator;
import com.databuff.apm.web.persistence.EventPersistence;
import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.AlarmSilenceStore;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.eval.RuleEvaluationResult;
import com.databuff.apm.web.portal.PortalTimeParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventRulePipeline {

    private final SingleMetricRuleEvaluator singleMetricRuleEvaluator;
    private final AlarmSilenceStore alarmSilenceStore;
    private final EventRecordFactory eventRecordFactory;
    private final EventPersistence eventPersistence;
    private final EventAlarmOpener eventAlarmOpener;
    private final AlarmResponseExecutor responseExecutor;
    private final long lookbackMillis;

    public EventRulePipeline(
            SingleMetricRuleEvaluator singleMetricRuleEvaluator,
            AlarmSilenceStore alarmSilenceStore,
            EventRecordFactory eventRecordFactory,
            EventPersistence eventPersistence,
            EventAlarmOpener eventAlarmOpener,
            AlarmResponseExecutor responseExecutor,
            @Value("${apm.alarm.lookback-minutes:5}") long lookbackMinutes) {
        this.singleMetricRuleEvaluator = singleMetricRuleEvaluator;
        this.alarmSilenceStore = alarmSilenceStore;
        this.eventRecordFactory = eventRecordFactory;
        this.eventPersistence = eventPersistence;
        this.eventAlarmOpener = eventAlarmOpener;
        this.responseExecutor = responseExecutor;
        this.lookbackMillis = lookbackMinutes * 60_000L;
    }

    public void evaluateRule(EventRule rule) {
        if (!rule.enabled()) {
            return;
        }
        if (alarmSilenceStore.isSilenced(rule.service())) {
            return;
        }
        List<RuleEvaluationResult> results = singleMetricRuleEvaluator.evaluateAll(rule, lookbackMillis);
        Instant eventBucketAt = PortalTimeParser.eventBucketNow();
        for (RuleEvaluationResult result : results) {
            if (!result.triggered()) {
                continue;
            }
            EventRecord eventRecord = eventRecordFactory.fromEvaluation(
                    rule.id(), rule.ruleName(), result.service(), result, false, eventBucketAt);
            eventPersistence.persist(eventRecord);
            eventAlarmOpener.openForEvent(eventRecord)
                    .ifPresent(alarm -> responseExecutor.dispatch(alarm, eventRecord));
        }
    }

    public List<EventRule> filterEnabledRules(List<EventRule> rules) {
        List<EventRule> filtered = new ArrayList<>();
        for (EventRule rule : rules) {
            if (rule.enabled()) {
                filtered.add(rule);
            }
        }
        return filtered;
    }
}
