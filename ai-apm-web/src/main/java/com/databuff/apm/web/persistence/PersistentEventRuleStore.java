package com.databuff.apm.web.persistence;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.EventRuleStore;
import com.databuff.apm.web.monitor.InMemoryEventRuleStore;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Primary
public class PersistentEventRuleStore implements EventRuleStore {

    private static final Logger log = LoggerFactory.getLogger(PersistentEventRuleStore.class);

    private final InMemoryEventRuleStore memoryStore;
    private final ApmReadRepository readRepository;
    private final String configDatabase;
    private volatile boolean persistenceEnabled;

    public PersistentEventRuleStore(
            InMemoryEventRuleStore memoryStore,
            ApmReadRepository readRepository,
            ApmStorageProperties storageProperties) {
        this.memoryStore = memoryStore;
        this.readRepository = readRepository;
        this.configDatabase = storageProperties.configDatabase();
    }

    void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.eventRuleSchemaReady()) {
            log.info("Monitor rule store not ready; monitor rules stay in-memory only");
            return;
        }
        try {
            List<ApmConfigRepository.EventRuleRow> rows = repository.loadEventRules();
            if (!rows.isEmpty()) {
                memoryStore.replaceAll(rows.stream().map(this::toRule).toList());
                long maxId = rows.stream().mapToLong(ApmConfigRepository.EventRuleRow::id).max().orElse(0);
                memoryStore.syncIdSequence(maxId + 1);
            }
            persistenceEnabled = true;
            log.info("Monitor rule persistence enabled ({} rows from store)", rows.size());
        } catch (Exception e) {
            log.warn("Failed to load monitor rules from store: {}", e.getMessage());
        }
    }

    @Override
    public List<EventRule> list() {
        return memoryStore.list();
    }

    @Override
    public Optional<EventRule> findById(long id) {
        return memoryStore.findById(id);
    }

    @Override
    public EventRule save(EventRule rule) {
        EventRule saved = memoryStore.save(rule);
        persist(saved);
        return saved;
    }

    @Override
    public boolean delete(long id) {
        boolean removed = memoryStore.delete(id);
        if (removed && persistenceEnabled) {
            try {
                new ApmConfigRepository(readRepository, configDatabase).deleteEventRule(id);
            } catch (Exception e) {
                log.warn("Failed to delete monitor rule {} from store: {}", id, e.getMessage());
            }
        }
        return removed;
    }

    @Override
    public EventRule create(CreateRequest request) {
        EventRule created = memoryStore.create(request);
        persist(created);
        return created;
    }

    private void persist(EventRule rule) {
        if (!persistenceEnabled) {
            return;
        }
        try {
            new ApmConfigRepository(readRepository, configDatabase).upsertEventRule(toRow(rule));
        } catch (Exception e) {
            log.warn("Failed to persist monitor rule {}: {}", rule.id(), e.getMessage());
        }
    }

    private EventRule toRule(ApmConfigRepository.EventRuleRow row) {
        return new EventRule(
                row.id(),
                row.ruleName(),
                row.classify(),
                row.detectionWay(),
                row.service(),
                row.metric(),
                row.threshold(),
                row.comparator(),
                row.enabled(),
                row.queryJson(),
                Instant.now());
    }

    private ApmConfigRepository.EventRuleRow toRow(EventRule rule) {
        return new ApmConfigRepository.EventRuleRow(
                rule.id(),
                rule.ruleName(),
                rule.classify(),
                rule.detectionWay(),
                rule.service(),
                rule.metric(),
                rule.threshold(),
                rule.comparator(),
                rule.enabled(),
                rule.queryJson());
    }
}
