package com.databuff.apm.web.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Loads Doris-backed config after the web port is listening so startup is not blocked on remote JDBC.
 */
@Component
public class PersistenceStartupHydrator {

    private static final Logger log = LoggerFactory.getLogger(PersistenceStartupHydrator.class);

    private final LlmProviderPersistence llmProviderPersistence;
    private final PersistentEventRuleStore eventRuleStore;
    private final AlarmPersistence alarmPersistence;
    private final AlarmSilencePersistence alarmSilencePersistence;
    private final AiSessionPersistence aiSessionPersistence;
    private final NotifyChannelPersistence notifyChannelPersistence;
    private final TrafficLightConfigPersistence trafficLightConfigPersistence;
    private final AiPlatformPersistence aiPlatformPersistence;
    private final EventPersistence eventPersistence;
    private final AlarmPolicyHydrator alarmPolicyHydrator;
    private final ExpertTaskPersistence expertTaskPersistence;
    private final MetricCorePersistence metricCorePersistence;

    public PersistenceStartupHydrator(
            LlmProviderPersistence llmProviderPersistence,
            PersistentEventRuleStore eventRuleStore,
            AlarmPersistence alarmPersistence,
            AlarmSilencePersistence alarmSilencePersistence,
            AiSessionPersistence aiSessionPersistence,
            NotifyChannelPersistence notifyChannelPersistence,
            TrafficLightConfigPersistence trafficLightConfigPersistence,
            AiPlatformPersistence aiPlatformPersistence,
            ExpertTaskPersistence expertTaskPersistence,
            EventPersistence eventPersistence,
            AlarmPolicyHydrator alarmPolicyHydrator,
            MetricCorePersistence metricCorePersistence) {
        this.llmProviderPersistence = llmProviderPersistence;
        this.eventRuleStore = eventRuleStore;
        this.alarmPersistence = alarmPersistence;
        this.alarmSilencePersistence = alarmSilencePersistence;
        this.aiSessionPersistence = aiSessionPersistence;
        this.notifyChannelPersistence = notifyChannelPersistence;
        this.trafficLightConfigPersistence = trafficLightConfigPersistence;
        this.aiPlatformPersistence = aiPlatformPersistence;
        this.expertTaskPersistence = expertTaskPersistence;
        this.eventPersistence = eventPersistence;
        this.alarmPolicyHydrator = alarmPolicyHydrator;
        this.metricCorePersistence = metricCorePersistence;
    }

    @EventListener(WebServerInitializedEvent.class)
    public void hydrateAsync(WebServerInitializedEvent event) {
        Thread worker = new Thread(this::hydrateAll, "persistence-hydrator");
        worker.setDaemon(true);
        worker.start();
        log.info("Persistence hydrate scheduled (web port {})", event.getWebServer().getPort());
    }

    void hydrateAll() {
        long started = System.currentTimeMillis();
        try {
            metricCorePersistence.reloadFromStore();
            trafficLightConfigPersistence.reloadFromStore();
            notifyChannelPersistence.reloadFromStore();
            eventRuleStore.reloadFromStore();
            alarmPolicyHydrator.reloadFromStore();
            eventPersistence.reloadFromStore();
            llmProviderPersistence.reloadFromStore();
            alarmSilencePersistence.reloadFromStore();
            alarmPersistence.reloadFromStore();
            aiSessionPersistence.reloadFromStore();
            aiPlatformPersistence.reloadFromStore();
            expertTaskPersistence.reloadFromStore();
            log.info("Persistence hydrate finished in {} ms", System.currentTimeMillis() - started);
        } catch (Exception e) {
            log.warn("Persistence hydrate failed: {}", e.getMessage());
        }
    }
}
