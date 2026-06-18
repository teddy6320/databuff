package com.databuff.apm.ingest.meta;

import com.databuff.apm.common.meta.MetaServiceInfo;
import com.databuff.apm.common.time.ApmTimeZones;
import com.databuff.apm.common.query.ApmQueryModels.MetaServicePoint;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.DorisBatchWriter;
import com.databuff.apm.common.storage.MetricQueryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory service catalog aligned with legacy {@code ServiceSyncService}:
 * <ul>
 *   <li>periodic pull from Doris {@code meta_service}</li>
 *   <li>{@code existById} / {@code existByName} caches</li>
 *   <li>{@code toInsert} / {@code toUpdate} staging before stream load</li>
 * </ul>
 */
public final class MetaServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(MetaServiceRegistry.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final DateTimeFormatter DATETIME = ApmTimeZones.WALL_CLOCK;

    private final ApmReadRepository reader;
    private final String database;
    private final long syncIntervalMs;

    private final ConcurrentHashMap<String, MetaServiceInfo> existById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MetaServiceInfo> existByName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MetaServiceInfo> toInsert = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MetaServiceInfo> toUpdate = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MetaServiceInfo> stagedRows = new ConcurrentHashMap<>();
    private final Set<String> touchIds = ConcurrentHashMap.newKeySet();
    private final Set<String> stagedIds = ConcurrentHashMap.newKeySet();

    private ScheduledExecutorService scheduler;

    public MetaServiceRegistry(ApmReadRepository reader, String database, long syncIntervalMs) {
        this.reader = Objects.requireNonNull(reader);
        this.database = database == null || database.isBlank() ? "databuff" : database.trim();
        this.syncIntervalMs = Math.max(5_000L, syncIntervalMs);
    }

    public void start() {
        syncFromStore();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "meta-service-sync");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(
                this::syncAndApplySafe,
                syncIntervalMs,
                syncIntervalMs,
                TimeUnit.MILLISECONDS);
        log.info("Meta service registry started database={} syncIntervalMs={}", database, syncIntervalMs);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    public Optional<MetaServiceInfo> getByServiceId(String serviceId) {
        if (serviceId == null || serviceId.isBlank()) {
            return Optional.empty();
        }
        MetaServiceInfo cached = existById.get(serviceId);
        if (cached != null) {
            return Optional.of(cached);
        }
        MetaServiceInfo pending = toInsert.get(serviceId);
        if (pending != null) {
            return Optional.of(pending);
        }
        MetaServiceInfo updating = toUpdate.get(serviceId);
        return Optional.ofNullable(updating);
    }

    public Optional<MetaServiceInfo> getByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(existByName.get(name));
    }

    public void remember(MetaServiceInfo incoming) {
        if (incoming == null || incoming.id() == null || incoming.id().isBlank()) {
            return;
        }
        String serviceId = incoming.id();
        MetaServiceInfo existing = existById.get(serviceId);
        if (existing == null) {
            MetaServiceInfo pending = toInsert.merge(serviceId, incoming, MetaServiceInfo::merge);
            stagedRows.put(serviceId, pending);
            stagedIds.remove(serviceId);
            return;
        }
        MetaServiceInfo merged = existing.merge(incoming);
        touchIds.add(serviceId);
        stagedRows.put(serviceId, merged);
        stagedIds.remove(serviceId);
        if (merged.enrichmentDiffers(existing)) {
            toUpdate.put(serviceId, merged);
        }
    }

    public int stagePending(DorisBatchWriter batchWriter) {
        if (stagedRows.isEmpty()) {
            return 0;
        }
        String updateTime = DATETIME.format(Instant.now());
        int staged = 0;
        for (Map.Entry<String, MetaServiceInfo> entry : stagedRows.entrySet()) {
            String serviceId = entry.getKey();
            if (!stagedIds.add(serviceId)) {
                continue;
            }
            if (offerRow(batchWriter, entry.getValue(), updateTime, serviceId)) {
                staged++;
            }
        }
        return staged;
    }

    public void onFlushComplete() {
        stagedIds.clear();
        touchIds.clear();
        toInsert.clear();
        toUpdate.clear();
        stagedRows.clear();
        syncFromStore();
    }

    public int cachedSize() {
        return existById.size();
    }

    private void syncAndApplySafe() {
        try {
            syncFromStore();
        } catch (Exception e) {
            log.warn("Meta service sync failed: {}", e.getMessage());
        }
    }

    private void syncFromStore() {
        try {
            String sql = MetricQueryBuilder.metaServicesSql(database, null);
            List<MetaServicePoint> rows = reader.queryMetaServices(sql);
            ConcurrentHashMap<String, MetaServiceInfo> byId = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, MetaServiceInfo> byName = new ConcurrentHashMap<>();
            for (MetaServicePoint point : rows) {
                MetaServiceInfo info = MetaServiceInfo.fromPoint(point);
                if (info == null) {
                    continue;
                }
                byId.put(info.id(), info);
                String displayName = info.name();
                if (displayName == null || displayName.isBlank()) {
                    displayName = info.service();
                }
                if (displayName != null && !displayName.isBlank()) {
                    byName.put(displayName, info);
                }
            }
            existById.clear();
            existById.putAll(byId);
            existByName.clear();
            existByName.putAll(byName);
        } catch (Exception e) {
            log.warn("Meta service sync query failed: {}", e.getMessage());
        }
    }

    private boolean offerRow(DorisBatchWriter batchWriter, MetaServiceInfo row, String updateTime, String serviceId) {
        try {
            batchWriter.offer(JSON.writeValueAsBytes(row.toRow(updateTime)));
            return true;
        } catch (JsonProcessingException e) {
            stagedIds.remove(serviceId);
            log.warn("Meta service row encode failed id={}: {}", serviceId, e.getMessage());
            return false;
        }
    }
}
