package com.databuff.apm.web.persistence;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.metric.MetricCoreCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MetricCorePersistence {

    private static final Logger log = LoggerFactory.getLogger(MetricCorePersistence.class);

    private final ApmReadRepository readRepository;
    private final String configDatabase;
    private final MetricCoreCatalogService catalogService;

    public MetricCorePersistence(
            ApmReadRepository readRepository,
            ApmStorageProperties storageProperties,
            MetricCoreCatalogService catalogService) {
        this.readRepository = readRepository;
        this.configDatabase = storageProperties.configDatabase();
        this.catalogService = catalogService;
    }

    public void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.metricCoreSchemaReady()) {
            log.error("config_metric_core table missing; apply deploy/common/sql/databuff.sql");
            return;
        }
        try {
            long rowCount = repository.countMetricCoreRows();
            if (rowCount == 0) {
                log.error("config_metric_core is empty; apply deploy/common/sql/databuff.sql seed data");
                return;
            }
            catalogService.reloadFromRepository(repository);
            log.info("Loaded {} config_metric_core rows into metric catalog", rowCount);
        } catch (Exception e) {
            log.error("Failed to load config_metric_core: {}", e.getMessage(), e);
        }
    }
}
