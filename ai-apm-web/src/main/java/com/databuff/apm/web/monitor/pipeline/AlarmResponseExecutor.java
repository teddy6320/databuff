package com.databuff.apm.web.monitor.pipeline;

import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.NotifyChannelService;
import com.databuff.apm.web.monitor.policy.AlarmFilterConditionMatcher;
import com.databuff.apm.web.monitor.policy.AlarmPolicySupport;
import com.databuff.apm.web.monitor.policy.ResponsePolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class AlarmResponseExecutor {

    private static final Logger log = LoggerFactory.getLogger(AlarmResponseExecutor.class);

    private final ResponsePolicyService responsePolicyService;
    private final NotifyChannelService notifyChannelService;

    public AlarmResponseExecutor(
            ResponsePolicyService responsePolicyService,
            NotifyChannelService notifyChannelService) {
        this.responsePolicyService = responsePolicyService;
        this.notifyChannelService = notifyChannelService;
    }

    public void dispatch(Alarm alarm, EventRecord event) {
        notifyChannelService.notifyAlert(alarm);
        for (Map<String, Object> policy : enabledPolicies()) {
            if (!matchesPolicy(policy, alarm, event)) {
                continue;
            }
            executeActions(policy, alarm);
        }
    }

    private List<Map<String, Object>> enabledPolicies() {
        Object list = responsePolicyService.list(Map.of("enabled", true, "pageNum", 1, "pageSize", 1000)).get("list");
        if (!(list instanceof List<?> rows)) {
            return List.of();
        }
        return rows.stream().map(item -> AlarmPolicySupport.copyOf((Map<String, Object>) item)).toList();
    }

    private static boolean matchesPolicy(Map<String, Object> policy, Alarm alarm, EventRecord event) {
        Object filterConditions = policy.get("filterConditions");
        if (!(filterConditions instanceof List<?> list) || list.isEmpty()) {
            return true;
        }
        return AlarmFilterConditionMatcher.matches(list, alarm, event);
    }

    @SuppressWarnings("unchecked")
    private void executeActions(Map<String, Object> policy, Alarm alarm) {
        Object actionsObj = policy.get("respActions");
        if (!(actionsObj instanceof List<?> actions)) {
            return;
        }
        for (Object actionObj : actions) {
            if (!(actionObj instanceof Map<?, ?> rawAction)) {
                continue;
            }
            Map<String, Object> action = (Map<String, Object>) rawAction;
            String type = String.valueOf(action.getOrDefault("type", "webhook"));
            if ("webhook".equalsIgnoreCase(type)) {
                postWebhook(String.valueOf(action.get("url")), alarm);
            }
        }
    }

    private void postWebhook(String url, Alarm alarm) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
            String body = """
                    {"alarmId":"%s","service":"%s","status":"%s","message":"%s"}
                    """.formatted(
                    escape(alarm.id()),
                    escape(alarm.service()),
                    escape(alarm.status()),
                    escape(alarm.message()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            log.debug("response webhook failed for alarm {}: {}", alarm.id(), e.toString());
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
