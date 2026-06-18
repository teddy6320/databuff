package com.databuff.apm.web.cockpit;

import com.databuff.apm.common.query.ApmQueryModels.TrafficLightPoint;

import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.MetricQueryBuilder;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrafficLightService {

    private final ApmReadRepository readRepository;
    private final String metricDatabase;
    private final Map<String, Object> config = new ConcurrentHashMap<>();

    public TrafficLightService(ApmReadRepository readRepository, ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.metricDatabase = storageProperties.metricDatabase();
        config.put("errorRateThreshold", 0.05);
        config.put("minRequestCount", 10);
        config.put("showServiceNumber", 10);
        config.put("alarmRed", 2);
        config.put("alarmYellow", 1);
        config.put("exceptionRed", 10);
        config.put("exceptionYellow", 2);
    }

    public List<TrafficLightPoint> trafficLight(long fromMillis, long toMillis) {
        try {
            String sql = MetricQueryBuilder.trafficLightSql(metricDatabase, fromMillis, toMillis);
            return readRepository.queryTrafficLight(sql);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getConfig() {
        return Map.copyOf(config);
    }

    public void setConfig(Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            if (value != null) {
                config.put(key, value);
            }
        });
    }
}
