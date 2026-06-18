package com.databuff.apm.web.persistence;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.monitor.policy.AlarmPolicySupport;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Write-path persistence for alarm policies. Startup hydration is handled by
 * {@link AlarmPolicyHydrator} so this class only depends on storage, not policy services.
 */
@Component
public class AlarmPolicyPersistence {

    public static final String TYPE_RESPONSE = "response";

    private static final Logger log = LoggerFactory.getLogger(AlarmPolicyPersistence.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ApmReadRepository readRepository;
    private final String configDatabase;
    private volatile boolean persistenceEnabled;

    public AlarmPolicyPersistence(ApmReadRepository readRepository, ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.configDatabase = storageProperties.configDatabase();
    }

    void enablePersistence() {
        persistenceEnabled = true;
    }

    public void persistResponse(Map<String, Object> policy) {
        persist(TYPE_RESPONSE, AlarmPolicySupport.intValue(policy.get("id"), -1), policy);
    }

    public void deleteResponse(int id) {
        delete(TYPE_RESPONSE, id);
    }

    private void persist(String type, long id, Map<String, Object> policy) {
        if (!persistenceEnabled || id <= 0) {
            return;
        }
        try {
            String bodyJson = MAPPER.writeValueAsString(AlarmPolicySupport.copyOf(policy));
            new ApmConfigRepository(readRepository, configDatabase).upsertAlarmPolicy(
                    new ApmConfigRepository.AlarmPolicyRow(
                            type,
                            id,
                            AlarmPolicySupport.stringValue(policy.get("policyName"), "policy"),
                            AlarmPolicySupport.boolValue(policy.get("enabled"), true),
                            bodyJson,
                            Instant.now()));
        } catch (Exception e) {
            log.warn("Failed to persist {} policy {}: {}", type, id, e.getMessage());
        }
    }

    private void delete(String type, long id) {
        if (!persistenceEnabled || id <= 0) {
            return;
        }
        try {
            new ApmConfigRepository(readRepository, configDatabase).deleteAlarmPolicy(type, id);
        } catch (Exception e) {
            log.warn("Failed to delete {} policy {}: {}", type, id, e.getMessage());
        }
    }
}
