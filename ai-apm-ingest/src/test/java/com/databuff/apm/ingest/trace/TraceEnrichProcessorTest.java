package com.databuff.apm.ingest.trace;

import com.databuff.apm.common.cluster.cache.CacheRegionPolicy;
import com.databuff.apm.common.cluster.cache.ClusterCacheRegistry;
import com.databuff.apm.common.model.DcSpan;
import com.databuff.apm.ingest.meta.IngestMetaCache;
import com.databuff.apm.ingest.meta.MetaServiceRegistry;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TraceEnrichProcessorTest {

    private MetaServiceRegistry serviceRegistry;

    @AfterEach
    void tearDown() {
        if (serviceRegistry != null) {
            serviceRegistry.stop();
        }
    }

    private IngestMetaCache newMetaCache(ClusterCacheRegistry registry) throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryMetaServices(anyString())).thenReturn(List.of());
        serviceRegistry = new MetaServiceRegistry(reader, "databuff", 60_000L);
        serviceRegistry.start();
        return new IngestMetaCache(registry, serviceRegistry);
    }

    @Test
    void fillsMissingHostAndResource() {
        DcSpan span = new DcSpan();
        span.trace_id = "t1";
        span.span_id = "s1";
        span.service = "api";
        span.name = "ping";
        span.start = 1L;
        span.end = 2L;

        DcSpan enriched = new TraceEnrichProcessor().enrich(span);
        assertThat(enriched.hostName).isEqualTo("unknown");
        assertThat(enriched.resource).isEqualTo("ping");
    }

    @Test
    void resolvesServiceFromMetaCache() throws Exception {
        ClusterCacheRegistry registry = new ClusterCacheRegistry();
        registry.region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1));
        IngestMetaCache metaCache = newMetaCache(registry);

        DcSpan seed = new DcSpan();
        seed.service = "checkout";
        seed.serviceId = "svc-1";
        seed.serviceInstance = "inst-a";
        metaCache.remember(seed);

        DcSpan span = new DcSpan();
        span.trace_id = "t2";
        span.span_id = "s2";
        span.serviceId = "svc-1";
        span.name = "ping";
        span.start = 1L;
        span.end = 2L;

        DcSpan enriched = new TraceEnrichProcessor(metaCache).enrich(span);
        assertThat(enriched.service).isEqualTo("checkout");
        assertThat(enriched.serviceInstance).isEqualTo("inst-a");
    }

    @Test
    void keepsBlankServiceWhenMetaCacheMisses() throws Exception {
        ClusterCacheRegistry registry = new ClusterCacheRegistry();
        registry.region("ingest.meta", CacheRegionPolicy.REPLICATED, Duration.ofHours(1));
        IngestMetaCache metaCache = newMetaCache(registry);

        DcSpan span = new DcSpan();
        span.trace_id = "t3";
        span.span_id = "s3";
        span.serviceId = "missing";
        span.name = "ping";
        span.start = 1L;
        span.end = 2L;

        DcSpan enriched = new TraceEnrichProcessor(metaCache).enrich(span);
        assertThat(enriched.service).isNull();
    }
}
