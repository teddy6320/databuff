package com.databuff.apm.web.persistence;

import com.databuff.apm.web.monitor.NotifyChannelService;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.springframework.stereotype.Component;

@Component
public class NotifyChannelPersistence {

    private static final Logger log = LoggerFactory.getLogger(NotifyChannelPersistence.class);

    private final ApmReadRepository readRepository;
    private final NotifyChannelService notifyChannelService;
    private final String configDatabase;
    private volatile boolean persistenceEnabled;

    public NotifyChannelPersistence(
            ApmReadRepository readRepository,
            NotifyChannelService notifyChannelService,
            ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.notifyChannelService = notifyChannelService;
        this.configDatabase = storageProperties.configDatabase();
    }

    void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.notifyChannelSchemaReady()) {
            log.info("Notify channel store not ready; notify config stays in-memory only");
            return;
        }
        try {
            repository.loadNotifyChannel().ifPresent(row -> notifyChannelService.applyPersistedRow(row));
            persistenceEnabled = true;
            log.info("Notify channel persistence enabled");
        } catch (Exception e) {
            log.warn("Failed to load notify channel from store: {}", e.getMessage());
        }
    }

    public void persist(String webhookUrl, boolean enabled) {
        if (!persistenceEnabled) {
            return;
        }
        try {
            new ApmConfigRepository(readRepository, configDatabase).upsertNotifyChannel(
                    new ApmConfigRepository.NotifyChannelRow(1L, "webhook", webhookUrl, enabled));
        } catch (Exception e) {
            log.warn("Failed to persist notify channel: {}", e.getMessage());
        }
    }

    boolean persistenceEnabled() {
        return persistenceEnabled;
    }
}
