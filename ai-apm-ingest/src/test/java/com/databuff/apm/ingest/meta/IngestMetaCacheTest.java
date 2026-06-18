package com.databuff.apm.ingest.meta;

import com.databuff.apm.common.cluster.cache.CacheRegionPolicy;
import com.databuff.apm.common.cluster.cache.ClusterCacheRegistry;
import com.databuff.apm.common.model.DcSpan;
import com.databuff.apm.common.query.ApmQueryModels.MetaServicePoint;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IngestMetaCacheTest {

    private MetaServiceRegistry serviceRegistry;

    @AfterEach
    void tearDown() {
        if (serviceRegistry != null) {
            serviceRegistry.stop();
        }
    }

    private IngestMetaCache newCache(ClusterCacheRegistry registry) throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryMetaServices(anyString())).thenReturn(List.of());
        serviceRegistry = new MetaServiceRegistry(reader, "databuff", 60_000L);
        serviceRegistry.start();
        return new IngestMetaCache(registry, serviceRegistry);
    }

    @Test
    void remembersAndLooksUpServiceMeta() throws Exception {
        ClusterCacheRegistry registry = new ClusterCacheRegistry();
        registry.region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1));
        IngestMetaCache cache = newCache(registry);

        DcSpan span = new DcSpan();
        span.service = "checkout";
        span.serviceId = "svc-1";
        span.serviceInstance = "inst-a";
        cache.remember(span);

        assertThat(cache.lookup("svc-1")).isPresent();
        assertThat(cache.lookup("svc-1").orElseThrow().service()).isEqualTo("checkout");
    }

    @Test
    void ignoresBlankServiceId() throws Exception {
        ClusterCacheRegistry registry = new ClusterCacheRegistry();
        registry.region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1));
        IngestMetaCache cache = newCache(registry);
        DcSpan span = new DcSpan();
        span.service = "checkout";
        cache.remember(span);
        assertThat(cache.lookup("")).isEmpty();
    }

    @Test
    void requiresRegisteredRegion() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryMetaServices(anyString())).thenReturn(List.of());
        serviceRegistry = new MetaServiceRegistry(reader, "databuff", 60_000L);
        assertThatThrownBy(() -> new IngestMetaCache(new ClusterCacheRegistry(), serviceRegistry))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void prefersRegistryCatalogOverLocalPayload() throws Exception {
        ClusterCacheRegistry registry = new ClusterCacheRegistry();
        registry.region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1))
                .put("svc:legacy", "local-only".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryMetaServices(anyString())).thenReturn(List.of(
                MetaServicePoint.minimal("legacy", "Catalog Name")));
        serviceRegistry = new MetaServiceRegistry(reader, "databuff", 60_000L);
        serviceRegistry.start();
        IngestMetaCache cache = new IngestMetaCache(registry, serviceRegistry);
        assertThat(cache.lookup("legacy").orElseThrow().service()).isEqualTo("Catalog Name");
    }
}
