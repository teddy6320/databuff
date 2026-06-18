package com.databuff.apm.web.monitor.policy;

import com.databuff.apm.web.persistence.AlarmPolicyPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ResponsePolicyService {

    private final Map<Integer, Map<String, Object>> policies = new ConcurrentHashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(1);
    @Autowired
    private AlarmPolicyPersistence alarmPolicyPersistence;

    public void replaceFromStore(Map<Integer, Map<String, Object>> loaded) {
        if (loaded == null || loaded.isEmpty()) {
            return;
        }
        policies.clear();
        policies.putAll(loaded);
        int maxId = loaded.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
        idSequence.set(maxId + 1);
    }

    public Map<String, Object> list(Map<String, Object> body) {
        Map<String, Object> params = body == null ? Map.of() : body;
        String keyword = AlarmPolicySupport.stringValue(params.get("policyName"), "");
        Object enabled = params.get("enabled");
        List<Map<String, Object>> all = policies.values().stream().map(AlarmPolicySupport::copyOf).toList();
        long total = AlarmPolicySupport.count(all, row ->
                AlarmPolicySupport.keywordMatch(row, keyword, "policyName")
                        && AlarmPolicySupport.enabledMatch(row, enabled));
        List<Map<String, Object>> page = AlarmPolicySupport.filterSortPage(
                all,
                params,
                row -> AlarmPolicySupport.keywordMatch(row, keyword, "policyName")
                        && AlarmPolicySupport.enabledMatch(row, enabled),
                "updatedTime");
        return AlarmPolicySupport.pageEnvelope(page, total);
    }

    public Optional<Map<String, Object>> findById(int id) {
        Map<String, Object> row = policies.get(id);
        return row == null ? Optional.empty() : Optional.of(AlarmPolicySupport.copyOf(row));
    }

    public Map<String, Object> save(Map<String, Object> body) {
        int id = AlarmPolicySupport.intValue(body.get("id"), -1);
        long now = AlarmPolicySupport.nowMillis();
        if (id > 0 && policies.containsKey(id)) {
            Map<String, Object> existing = policies.get(id);
            mergeEditableFields(existing, body, now);
            if (alarmPolicyPersistence != null) {
                alarmPolicyPersistence.persistResponse(existing);
            }
            return AlarmPolicySupport.copyOf(existing);
        }
        int newId = idSequence.getAndIncrement();
        Map<String, Object> created = basePolicy(
                newId,
                AlarmPolicySupport.stringValue(body.get("policyName"), "响应策略"),
                AlarmPolicySupport.boolValue(body.get("enabled"), true),
                now);
        mergeEditableFields(created, body, now);
        policies.put(newId, created);
        if (alarmPolicyPersistence != null) {
            alarmPolicyPersistence.persistResponse(created);
        }
        return AlarmPolicySupport.copyOf(created);
    }

    public boolean delete(List<Integer> ids) {
        boolean deleted = false;
        for (Integer id : ids) {
            if (policies.remove(id) != null) {
                if (alarmPolicyPersistence != null) {
                    alarmPolicyPersistence.deleteResponse(id);
                }
                deleted = true;
            }
        }
        return deleted;
    }

    public void publish(List<Integer> ids, boolean enabled) {
        long now = AlarmPolicySupport.nowMillis();
        for (Integer id : ids) {
            Map<String, Object> row = policies.get(id);
            if (row != null) {
                row.put("enabled", enabled);
                row.put("updatedTime", now);
                if (alarmPolicyPersistence != null) {
                    alarmPolicyPersistence.persistResponse(row);
                }
            }
        }
    }

    public List<Map<String, Object>> export(Map<String, Object> body) {
        Map<String, Object> params = body == null ? Map.of() : body;
        List<Integer> ids = AlarmPolicySupport.intList(params.get("ids"));
        if (!ids.isEmpty()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Integer id : ids) {
                findById(id).ifPresent(rows::add);
            }
            return rows;
        }
        Object list = list(params).get("list");
        if (list instanceof List<?> rows) {
            return rows.stream().map(item -> AlarmPolicySupport.copyOf((Map<String, Object>) item)).toList();
        }
        return List.of();
    }

    private static Map<String, Object> basePolicy(int id, String name, boolean enabled, long now) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("policyName", name);
        row.put("enabled", enabled);
        row.put("actionType", 1);
        row.put("filterConditions", List.of());
        row.put("respConditions", List.of());
        row.put("respActions", List.of());
        row.put("creator", "admin");
        row.put("editor", "admin");
        row.put("createdTime", now);
        row.put("updatedTime", now);
        return row;
    }

    private static void mergeEditableFields(Map<String, Object> target, Map<String, Object> body, long now) {
        if (body.containsKey("policyName")) {
            target.put("policyName", AlarmPolicySupport.stringValue(body.get("policyName"), "响应策略"));
        }
        if (body.containsKey("enabled")) {
            target.put("enabled", AlarmPolicySupport.boolValue(body.get("enabled"), true));
        }
        if (body.containsKey("actionType")) {
            target.put("actionType", AlarmPolicySupport.intValue(body.get("actionType"), 1));
        }
        if (body.containsKey("filterConditions")) {
            target.put("filterConditions", body.get("filterConditions"));
        }
        if (body.containsKey("respConditions")) {
            target.put("respConditions", body.get("respConditions"));
        }
        if (body.containsKey("respActions")) {
            target.put("respActions", body.get("respActions"));
        }
        target.put("updatedTime", now);
    }
}
