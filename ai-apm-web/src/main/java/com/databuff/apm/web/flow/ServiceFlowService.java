package com.databuff.apm.web.flow;

import com.databuff.apm.common.query.ApmQueryModels.ServiceFlowEdge;

import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.MetricQueryBuilder;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ServiceFlowService {

    private final ApmReadRepository readRepository;
    private final String metricDatabase;

    public ServiceFlowService(ApmReadRepository readRepository, ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.metricDatabase = storageProperties.metricDatabase();
    }

    public List<ServiceFlowEdge> listFlows(String service, long fromMillis, long toMillis, int limit) {
        try {
            String sql = MetricQueryBuilder.serviceFlowSql(metricDatabase, service, fromMillis, toMillis, limit);
            return readRepository.queryServiceFlow(sql);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
