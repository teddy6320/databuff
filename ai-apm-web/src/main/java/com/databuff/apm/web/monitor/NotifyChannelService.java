package com.databuff.apm.web.monitor;

import com.databuff.apm.web.persistence.NotifyChannelPersistence;
import com.databuff.apm.common.storage.ApmConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotifyChannelService {

    @Autowired
    private ObjectProvider<NotifyChannelPersistence> persistence;
    private final Map<String, Object> config = new ConcurrentHashMap<>();

    @PostConstruct
    void initDefaults() {
        config.put("webhookUrl", "");
        config.put("enabled", false);
    }

    public Map<String, Object> getConfig() {
        return Map.copyOf(config);
    }

    public Map<String, Object> updateConfig(Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            if (value != null) {
                config.put(key, value);
            }
        });
        ifAvailable(sync -> sync.persist(
                String.valueOf(config.getOrDefault("webhookUrl", "")),
                Boolean.TRUE.equals(config.get("enabled"))));
        return getConfig();
    }

    public void applyPersistedRow(ApmConfigRepository.NotifyChannelRow row) {
        config.put("webhookUrl", row.webhookUrl() == null ? "" : row.webhookUrl());
        config.put("enabled", row.enabled());
    }

    public void notifyAlert(Alarm event) {
        if (!Boolean.TRUE.equals(config.get("enabled"))) {
            return;
        }
        Object urlObj = config.get("webhookUrl");
        if (urlObj == null || String.valueOf(urlObj).isBlank()) {
            return;
        }
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
            String body = """
                    {"alarmId":"%s","service":"%s","status":"%s","message":"%s"}
                    """.formatted(
                    escape(event.id()),
                    escape(event.service()),
                    escape(event.status()),
                    escape(event.message()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.valueOf(urlObj)))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            // best effort webhook
        }
    }

    private void ifAvailable(java.util.function.Consumer<NotifyChannelPersistence> consumer) {
        if (persistence != null) {
            persistence.ifAvailable(consumer);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
