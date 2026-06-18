package com.databuff.apm.web.persistence;

import com.databuff.apm.web.monitor.AlarmSilenceStore;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class AlarmSilencePersistence {

    private static final Logger log = LoggerFactory.getLogger(AlarmSilencePersistence.class);

    private final ApmReadRepository readRepository;
    private final AlarmSilenceStore silenceStore;
    private final String configDatabase;
    private volatile boolean persistenceEnabled;

    public AlarmSilencePersistence(
            ApmReadRepository readRepository,
            AlarmSilenceStore silenceStore,
            ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.silenceStore = silenceStore;
        this.configDatabase = storageProperties.configDatabase();
    }

    void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.alarmSilenceSchemaReady()) {
            log.info("Alert silence store not ready; alert silence stays in-memory only");
            return;
        }
        try {
            List<ApmConfigRepository.AlarmSilenceRow> rows = repository.loadActiveAlarmSilences();
            for (ApmConfigRepository.AlarmSilenceRow row : rows) {
                silenceStore.restore(row.service(), row.silencedUntil());
            }
            persistenceEnabled = true;
            log.info("Alert silence persistence enabled ({} rows from store)", rows.size());
        } catch (Exception e) {
            log.warn("Failed to load alert silence from store: {}", e.getMessage());
        }
    }

    public void persistSilence(String service, Instant silencedUntil) {
        if (!persistenceEnabled) {
            return;
        }
        try {
            new ApmConfigRepository(readRepository, configDatabase).upsertAlarmSilence(
                    new ApmConfigRepository.AlarmSilenceRow(service, silencedUntil, Instant.now()));
        } catch (Exception e) {
            log.warn("Failed to persist alert silence for {}: {}", service, e.getMessage());
        }
    }

    boolean persistenceEnabled() {
        return persistenceEnabled;
    }
}
