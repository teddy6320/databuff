package com.databuff.apm.web.monitor;

import com.databuff.apm.web.persistence.AlarmSilencePersistence;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlarmSilenceStore {

    @Autowired
    private ObjectProvider<AlarmSilencePersistence> persistence;
    private final Map<String, Instant> silencedUntil = new ConcurrentHashMap<>();

    public void silenceService(String service, long durationMinutes) {
        if (service == null || service.isBlank() || durationMinutes <= 0) {
            return;
        }
        Instant until = Instant.now().plusSeconds(durationMinutes * 60L);
        silencedUntil.put(service, until);
        ifAvailable(sync -> sync.persistSilence(service, until));
    }

    public boolean isSilenced(String service) {
        if (service == null || service.isBlank()) {
            return false;
        }
        Instant until = silencedUntil.get(service);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            silencedUntil.remove(service);
            return false;
        }
        return true;
    }

    public void restore(String service, Instant until) {
        if (service != null && !service.isBlank() && until != null && until.isAfter(Instant.now())) {
            silencedUntil.put(service, until);
        }
    }

    private void ifAvailable(java.util.function.Consumer<AlarmSilencePersistence> consumer) {
        if (persistence != null) {
            persistence.ifAvailable(consumer);
        }
    }
}
