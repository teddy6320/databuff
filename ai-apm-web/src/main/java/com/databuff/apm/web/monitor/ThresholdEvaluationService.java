package com.databuff.apm.web.monitor;

import com.databuff.apm.common.query.ApmQueryModels.ErrorRateSnapshot;

import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.MetricQueryBuilder;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.portal.PortalTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThresholdEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(ThresholdEvaluationService.class);

    private final ApmReadRepository readRepository;
    private final String metricDatabase;

    public ThresholdEvaluationService(ApmReadRepository readRepository, ApmStorageProperties storageProperties) {
        this.readRepository = readRepository;
        this.metricDatabase = storageProperties.metricDatabase();
    }

    public double currentErrorRate(String service, long lookbackMillis) {
        long[] range = PortalTimeParser.metricEvaluationRange(lookbackMillis);
        return errorRateBetween(service, range[0], range[1]);
    }

    public double errorRateBetween(String service, long fromMillis, long toMillis) {
        long[] range = PortalTimeParser.normalizeMetricQueryRange(fromMillis, toMillis);
        fromMillis = range[0];
        toMillis = range[1];
        try {
            String sql = MetricQueryBuilder.serviceErrorRateSql(metricDatabase, service, fromMillis, toMillis);
            ErrorRateSnapshot snapshot = readRepository.queryErrorRate(sql);
            return snapshot.errorRate();
        } catch (Exception e) {
            log.debug("error rate query failed for {}: {}", service, e.toString());
            return 0;
        }
    }

    public long requestCountBetween(String service, long fromMillis, long toMillis) {
        long[] range = PortalTimeParser.normalizeMetricQueryRange(fromMillis, toMillis);
        fromMillis = range[0];
        toMillis = range[1];
        try {
            String sql = MetricQueryBuilder.serviceRequestCountSql(metricDatabase, service, fromMillis, toMillis);
            return readRepository.queryRequestCount(sql);
        } catch (Exception e) {
            log.debug("request count query failed for {}: {}", service, e.toString());
            return 0;
        }
    }
}
