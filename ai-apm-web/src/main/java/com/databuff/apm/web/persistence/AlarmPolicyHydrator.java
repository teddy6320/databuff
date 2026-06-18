package com.databuff.apm.web.persistence;

import com.databuff.apm.common.storage.ApmConfigRepository;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.monitor.policy.AlarmPolicySupport;
import com.databuff.apm.web.monitor.policy.ResponsePolicyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads alarm policies from Doris into in-memory policy services at startup.
 * Kept separate from {@link AlarmPolicyPersistence} so write-path persistence does not
 * depend on policy services (avoids circular bean dependencies).
 */
@Component
public class AlarmPolicyHydrator {

    private static final Logger log = LoggerFactory.getLogger(AlarmPolicyHydrator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ApmReadRepository readRepository;
    private final ResponsePolicyService responsePolicyService;
    private final AlarmPolicyPersistence alarmPolicyPersistence;
    private final String configDatabase;

    public AlarmPolicyHydrator(
            ApmReadRepository readRepository,
            ResponsePolicyService responsePolicyService,
            AlarmPolicyPersistence alarmPolicyPersistence,
            ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.responsePolicyService = responsePolicyService;
        this.alarmPolicyPersistence = alarmPolicyPersistence;
        this.configDatabase = storageProperties.configDatabase();
    }

    void reloadFromStore() {
        ApmConfigRepository repository = new ApmConfigRepository(readRepository, configDatabase);
        if (!repository.alarmPolicySchemaReady()) {
            log.info("Alarm policy store not ready; policies stay in-memory only");
            return;
        }
        try {
            responsePolicyService.replaceFromStore(loadPolicies(repository, AlarmPolicyPersistence.TYPE_RESPONSE));
            alarmPolicyPersistence.enablePersistence();
            log.info("Alarm policy persistence enabled");
        } catch (Exception e) {
            log.warn("Failed to load alarm policies from store: {}", e.getMessage());
        }
    }

    private static Map<Integer, Map<String, Object>> loadPolicies(ApmConfigRepository repository, String type)
            throws Exception {
        Map<Integer, Map<String, Object>> loaded = new LinkedHashMap<>();
        for (ApmConfigRepository.AlarmPolicyRow row : repository.loadAlarmPolicies(type)) {
            Map<String, Object> body = MAPPER.readValue(row.bodyJson(), new TypeReference<>() {
            });
            loaded.put((int) row.policyId(), AlarmPolicySupport.copyOf(body));
        }
        return loaded;
    }
}
