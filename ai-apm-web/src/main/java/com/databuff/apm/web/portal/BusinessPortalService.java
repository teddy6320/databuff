package com.databuff.apm.web.portal;

import com.databuff.apm.common.query.ApmQueryModels.ServiceSummaryPoint;
import com.databuff.apm.common.query.ApmQueryModels.TopologyEdge;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.MetricQueryBuilder;
import com.databuff.apm.common.util.PortalServiceIdResolver;
import com.databuff.apm.web.config.ApmStorageProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BusinessPortalService {

    private final ApmReadRepository readRepository;
    private final String metricDatabase;
    private final GlobalTopologyQueryService globalTopologyQueryService;

    public BusinessPortalService(
            ApmReadRepository readRepository,
            ApmStorageProperties storageProperties,
            GlobalTopologyQueryService globalTopologyQueryService) {
        this.readRepository = readRepository;
        this.metricDatabase = storageProperties.metricDatabase();
        this.globalTopologyQueryService = globalTopologyQueryService;
    }

    public Map<String, Object> callInfo(Map<String, Object> body) {
        long now = System.currentTimeMillis();
        long from = PortalTimeParser.rangeFrom(body, now - 3_600_000L);
        long to = PortalTimeParser.rangeTo(body, now);
        String srcServiceId = resolveServiceId(body, "srcServiceId", "srcSid");
        String dstServiceId = resolveServiceId(body, "dstServiceId", "dstSid");

        List<TopologyEdge> edges = filterBusinessCallEdges(loadTopologyEdges(from, to, 500), body);
        long reqOutCnt = 0;
        long reqOutErrCnt = 0;
        for (TopologyEdge edge : edges) {
            reqOutCnt += edge.callCount();
            reqOutErrCnt += edge.errorCount();
        }

        double reqOutErrRate = reqOutCnt > 0 ? (double) reqOutErrCnt / reqOutCnt : 0;
        long reqOutAvgLatency = weightedAvgLatencyNs(srcServiceId, from, to);
        long reqInAvgLatency = weightedAvgLatencyNs(dstServiceId, from, to);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("srcServiceId", srcServiceId);
        data.put("srcServiceName", srcServiceId);
        data.put("srcServiceType", "web");
        data.put("dstServiceId", dstServiceId);
        data.put("dstServiceName", dstServiceId);
        data.put("dstServiceType", "web");
        data.put("reqOutCnt", reqOutCnt);
        data.put("reqOutErrCnt", reqOutErrCnt);
        data.put("reqOutAvgLatency", reqOutAvgLatency);
        data.put("reqOutErrRate", reqOutErrRate);
        data.put("reqInCnt", reqOutCnt);
        data.put("reqInErrCnt", reqOutErrCnt);
        data.put("reqInAvgLatency", reqInAvgLatency);
        data.put("reqInErrRate", reqOutErrRate);
        return data;
    }

    public Map<String, Object> callEndpoints(Map<String, Object> body) {
        long now = System.currentTimeMillis();
        long from = PortalTimeParser.rangeFrom(body, now - 3_600_000L);
        long to = PortalTimeParser.rangeTo(body, now);
        int offset = ServicePortalService.intValue(body.get("offset"), 0);
        int size = ServicePortalService.intValue(body.get("size"), 50);
        String sortField = ServicePortalService.stringValue(body.get("sortField"), "reqOutCnt");
        String sortOrder = ServicePortalService.stringValue(body.get("sortOrder"), "desc");

        List<Map<String, Object>> rows = filterBusinessCallEdges(loadTopologyEdges(from, to, 500), body).stream()
                .map(this::toBusinessCallRow)
                .toList();
        rows = new ArrayList<>(rows);
        sortRows(rows, sortField, sortOrder);

        int total = rows.size();
        int end = Math.min(offset + size, total);
        List<Map<String, Object>> page = offset >= total ? List.of() : rows.subList(offset, end);

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("status", 200);
        envelope.put("message", "success");
        envelope.put("data", page);
        envelope.put("total", total);
        return envelope;
    }

    private Map<String, Object> toBusinessCallRow(TopologyEdge edge) {
        long reqCnt = edge.callCount();
        long errCnt = edge.errorCount();
        double errRate = reqCnt > 0 ? (double) errCnt / reqCnt : 0;
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("srcServiceId", edge.srcService());
        row.put("srcServiceName", edge.srcService());
        row.put("srcServiceType", "web");
        row.put("dstServiceId", edge.dstService());
        row.put("dstServiceName", edge.dstService());
        row.put("dstServiceType", "web");
        row.put("reqOutCnt", reqCnt);
        row.put("reqOutErrCnt", errCnt);
        row.put("reqOutAvgLatency", 0);
        row.put("reqOutErrRate", errRate);
        row.put("reqInCnt", reqCnt);
        row.put("reqInErrCnt", errCnt);
        row.put("reqInAvgLatency", 0);
        row.put("reqInErrRate", errRate);
        row.put("componentTypes", List.of("service.http"));
        return row;
    }

    private long weightedAvgLatencyNs(String serviceId, long from, long to) {
        if (serviceId == null || serviceId.isBlank()) {
            return 0L;
        }
        try {
            List<ServiceSummaryPoint> summaries = readRepository.queryServiceSummaries(
                    MetricQueryBuilder.serviceSummarySql(
                            metricDatabase,
                            from,
                            to,
                            "callCnt",
                            "desc",
                            0,
                            500));
            for (ServiceSummaryPoint summary : summaries) {
                if (PortalServiceIdResolver.matches(serviceId, summary.serviceId())
                        && summary.requestCount() > 0) {
                    return (long) (summary.sumDurationNs() / summary.requestCount());
                }
            }
        } catch (Exception ignored) {
            // optional enrichment
        }
        return 0L;
    }

    private List<TopologyEdge> filterBusinessCallEdges(List<TopologyEdge> edges, Map<String, Object> body) {
        String srcServiceId = resolveServiceId(body, "srcServiceId", "srcSid");
        String dstServiceId = resolveServiceId(body, "dstServiceId", "dstSid");
        String srcServiceQuery = ServicePortalService.stringValue(body.get("srcServiceQuery"), "");
        String dstServiceQuery = ServicePortalService.stringValue(body.get("dstServiceQuery"), "");
        String srcKeyword = srcServiceQuery == null ? "" : srcServiceQuery.toLowerCase(Locale.ROOT);
        String dstKeyword = dstServiceQuery == null ? "" : dstServiceQuery.toLowerCase(Locale.ROOT);

        return edges.stream()
                .filter(edge -> srcServiceId == null || srcServiceId.isBlank() || srcServiceId.equals(edge.srcService()))
                .filter(edge -> dstServiceId == null || dstServiceId.isBlank() || dstServiceId.equals(edge.dstService()))
                .filter(edge -> srcKeyword.isBlank()
                        || edge.srcService().toLowerCase(Locale.ROOT).contains(srcKeyword))
                .filter(edge -> dstKeyword.isBlank()
                        || edge.dstService().toLowerCase(Locale.ROOT).contains(dstKeyword))
                .toList();
    }

    private static String resolveServiceId(Map<String, Object> body, String primaryKey, String fallbackKey) {
        String value = ServicePortalService.stringValue(body.get(primaryKey), null);
        if (value == null) {
            value = ServicePortalService.stringValue(body.get(fallbackKey), null);
        }
        return value == null ? "" : value;
    }

    private static void sortRows(List<Map<String, Object>> rows, String sortField, String sortOrder) {
        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                row -> row.get(sortField),
                (left, right) -> compareValues(left, right, sortOrder));
        rows.sort(comparator);
    }

    private static int compareValues(Object left, Object right, String sortOrder) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        int cmp;
        if (left instanceof Number ln && right instanceof Number rn) {
            cmp = Double.compare(ln.doubleValue(), rn.doubleValue());
        } else {
            cmp = String.valueOf(left).compareTo(String.valueOf(right));
        }
        return "asc".equalsIgnoreCase(sortOrder) ? cmp : -cmp;
    }

    private List<TopologyEdge> loadTopologyEdges(long from, long to, int limit) {
        return globalTopologyQueryService.listTopologyEdges(from, to, limit);
    }
}
