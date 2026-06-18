package com.databuff.apm.web.persistence;

import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.AlarmStore;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlarmPersistence {

    private static final Logger log = LoggerFactory.getLogger(AlarmPersistence.class);
    private static final int HYDRATE_LIMIT = 200;

    private final ApmReadRepository readRepository;
    private final AlarmStore eventStore;
    private final String configDatabase;
    private volatile boolean persistenceEnabled;

    public AlarmPersistence(
            ApmReadRepository readRepository,
            AlarmStore eventStore,
            ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.eventStore = eventStore;
        this.configDatabase = storageProperties.configDatabase();
    }

    void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.alarmSchemaReady()) {
            log.info("Alert event store not ready; alert events stay in-memory only");
            return;
        }
        try {
            List<ApmConfigRepository.AlarmRow> rows = repository.loadRecentAlarms(HYDRATE_LIMIT);
            if (!rows.isEmpty()) {
                eventStore.replaceAll(rows.stream().map(this::toEvent).toList());
            }
            persistenceEnabled = true;
            log.info("Alert event persistence enabled ({} rows from store)", rows.size());
        } catch (Exception e) {
            log.warn("Failed to load alert events from store: {}", e.getMessage());
        }
    }

    public void persist(Alarm event) {
        if (!persistenceEnabled) {
            return;
        }
        try {
            new ApmConfigRepository(readRepository, configDatabase).upsertAlarm(toRow(event));
        } catch (Exception e) {
            log.warn("Failed to persist alert event {}: {}", event.id(), e.getMessage());
        }
    }

    boolean persistenceEnabled() {
        return persistenceEnabled;
    }

    private Alarm toEvent(ApmConfigRepository.AlarmRow row) {
        return new Alarm(
                row.id(),
                row.policyId(),
                row.service(),
                row.detectionWay(),
                row.level(),
                row.message(),
                row.status(),
                row.triggeredAt(),
                row.resolvedAt());
    }

    private ApmConfigRepository.AlarmRow toRow(Alarm event) {
        return new ApmConfigRepository.AlarmRow(
                event.id(),
                event.policyId(),
                event.service(),
                event.detectionWay(),
                event.level(),
                event.message(),
                event.status(),
                event.triggeredAt(),
                event.resolvedAt());
    }
}
