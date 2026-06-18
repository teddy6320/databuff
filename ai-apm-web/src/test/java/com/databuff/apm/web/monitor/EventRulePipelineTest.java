package com.databuff.apm.web.monitor;

import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.ai.TestBeanSupport;
import com.databuff.apm.web.monitor.eval.SingleMetricRuleEvaluator;
import com.databuff.apm.web.monitor.eval.ThresholdAlarmMessageFormatter;
import com.databuff.apm.web.monitor.policy.ResponsePolicyService;
import com.databuff.apm.web.persistence.EventPersistence;
import com.databuff.apm.common.query.ApmQueryModels.ErrorRateSnapshot;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.metric.MetricCoreCatalogService;
import com.databuff.apm.web.monitor.pipeline.AlarmResponseExecutor;
import com.databuff.apm.web.monitor.pipeline.EventAlarmOpener;
import com.databuff.apm.web.monitor.pipeline.EventRulePipeline;
import com.databuff.apm.web.monitor.pipeline.EventRecord;
import com.databuff.apm.web.monitor.pipeline.EventRecordFactory;
import com.databuff.apm.web.portal.PortalTimeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventRulePipelineTest {

    private InMemoryEventRuleStore ruleStore;
    private AlarmStore alarmStore;
    private EventRulePipeline monitorPipeline;
    private EventRuleService eventRuleService;
    private EventPersistence eventPersistence;

    @BeforeEach
    void setUp() throws Exception {
        ruleStore = new InMemoryEventRuleStore();
        alarmStore = new AlarmStore(TestMonitorRecordIds.create());
        AlarmSilenceStore alarmSilenceStore = new AlarmSilenceStore();
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryErrorRate(anyString())).thenReturn(new ErrorRateSnapshot(10, 100));
        when(reader.queryRequestCount(anyString())).thenReturn(0L);
        eventRuleService = new EventRuleService(ruleStore);
        ThresholdEvaluationService evaluationService = new ThresholdEvaluationService(reader, TestStorageSupport.storage());
        RuleMetricEvaluationService ruleMetricEvaluationService = new RuleMetricEvaluationService(
                reader, TestStorageSupport.storage(), new MetricCoreCatalogService());
        MetricCoreCatalogService metricCoreCatalogService = new MetricCoreCatalogService();
        ThresholdAlarmMessageFormatter messageFormatter = new ThresholdAlarmMessageFormatter(metricCoreCatalogService);
        SingleMetricRuleEvaluator singleMetricRuleEvaluator = new SingleMetricRuleEvaluator(
                evaluationService, ruleMetricEvaluationService, messageFormatter);
        EventRecordFactory eventRecordFactory = new EventRecordFactory(TestMonitorRecordIds.create());
        eventPersistence = mock(EventPersistence.class);
        when(eventPersistence.isPersistenceEnabled()).thenReturn(true);
        EventAlarmOpener eventAlarmOpener = TestBeanSupport.eventAlarmOpener(
                alarmStore, eventPersistence);
        ResponsePolicyService responsePolicyService = new ResponsePolicyService();
        AlarmResponseExecutor responseExecutor = new AlarmResponseExecutor(
                responsePolicyService, TestBeanSupport.notifyChannelService());
        monitorPipeline = new EventRulePipeline(
                singleMetricRuleEvaluator,
                alarmSilenceStore,
                eventRecordFactory,
                eventPersistence,
                eventAlarmOpener,
                responseExecutor,
                5);
    }

    @Test
    void opensAlertWhenThresholdBreached() {
        EventRule rule = eventRuleService.createRule(new EventRuleStore.CreateRequest(
                "checkout error rate", "checkout", 0.05, EventRule.COMPARATOR_GT, true));
        monitorPipeline.evaluateRule(rule);
        assertThat(alarmStore.findOpenByService("checkout")).isEmpty();
        assertThat(alarmStore.listRecent(1)).hasSize(1);
    }

    @Test
    void createdRuleProducesRawEventAndConvergesToOpenAlert() {
        EventRule created = eventRuleService.createRule(new EventRuleStore.CreateRequest(
                "checkout error rate",
                "checkout",
                0.05,
                EventRule.COMPARATOR_GT,
                true));

        monitorPipeline.evaluateRule(created);

        ArgumentCaptor<EventRecord> eventCaptor = ArgumentCaptor.forClass(EventRecord.class);
        verify(eventPersistence).persist(eventCaptor.capture());
        EventRecord eventRecord = eventCaptor.getValue();
        assertThat(eventRecord.id()).startsWith("E");
        assertThat(eventRecord.ruleId()).isEqualTo(created.id());
        assertThat(eventRecord.status()).isEqualTo(EventRecord.STATUS_TRIGGER);
        assertThat(eventRecord.service()).isEqualTo("checkout");
        assertThat(eventRecord.triggeredAt()).isEqualTo(PortalTimeParser.eventBucketNow());

        assertThat(alarmStore.listRecent(1))
                .hasSize(1)
                .first()
                .satisfies(alert -> {
                    assertThat(alert.status()).isEqualTo(Alarm.STATUS_RESOLVED);
                    assertThat(alert.level()).isEqualTo("critical");
                    assertThat(alert.triggeredAt()).isEqualTo(PortalTimeParser.eventBucketNow());
                    assertThat(alert.resolvedAt()).isEqualTo(PortalTimeParser.eventBucketNow());
                });
    }

}
