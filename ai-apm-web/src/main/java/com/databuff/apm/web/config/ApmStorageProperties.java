package com.databuff.apm.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Logical APM database names used by query and config persistence (storage-backend agnostic). */
@ConfigurationProperties(prefix = "apm.doris")
public record ApmStorageProperties(
        String metricDatabase,
        String traceDatabase,
        String configDatabase) {

    public ApmStorageProperties {
        if (metricDatabase == null || metricDatabase.isBlank()) {
            metricDatabase = "databuff";
        }
        if (traceDatabase == null || traceDatabase.isBlank()) {
            traceDatabase = "databuff";
        }
        if (configDatabase == null || configDatabase.isBlank()) {
            configDatabase = "databuff";
        }
    }
}
