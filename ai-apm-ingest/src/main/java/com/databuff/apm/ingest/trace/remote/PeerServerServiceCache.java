package com.databuff.apm.ingest.trace.remote;

import com.databuff.apm.common.meta.OtelAttributeMaps;
import com.databuff.apm.common.model.DcSpan;
import com.databuff.apm.common.serde.DcSpanUtil;

import java.util.Map;

/**
 * Peer address to downstream service cache; legacy portal compatibility with
 * {@code HttpRpcClientOutProcessor#cacheAddrServerSvcs}.
 */
public final class PeerServerServiceCache {

    private static final long TTL_MS = 24L * 60L * 60L * 1000L;
    private static final int MAX_SIZE = 20_000;

    private final ExpiringCache<String> cache = new ExpiringCache<>(TTL_MS, MAX_SIZE);

    public void cacheFillServerService(DcSpan span) {
        if (span == null || !"SPAN_KIND_CLIENT".equals(span.type)) {
            return;
        }
        if (!DcSpanUtil.isHttpSpan(span) && !DcSpanUtil.isRpcSpan(span)) {
            return;
        }
        Map<String, String> meta = OtelAttributeMaps.parse(span);
        String dataSource = TraceDataSources.resolve(meta);
        String cacheKey = cacheKey(span, meta, dataSource);
        if (cacheKey == null) {
            return;
        }
        String serverService = OtelAttributeMaps.firstNonBlank(meta, "server.service");
        if (serverService != null && !serverService.isBlank()) {
            cache.put(cacheKey, serverService.trim());
            return;
        }
        if (span.error == 0) {
            return;
        }
        String cached = cache.getIfPresent(cacheKey);
        if (cached == null || cached.isBlank()) {
            return;
        }
        meta = new java.util.LinkedHashMap<>(meta);
        meta.put("server.service", cached);
        OtelAttributeMaps.replace(span, meta);
    }

    private static String cacheKey(DcSpan span, Map<String, String> meta, String dataSource) {
        if (DcSpanUtil.isHttpSpan(span)) {
            RemotePeerAddressUtil.RemotePeer peer = RemotePeerAddressUtil.resolve(span, meta);
            if (peer == null) {
                return null;
            }
            return dataSource + ":" + peer.hostname() + ":" + peer.port();
        }
        if (DcSpanUtil.isRpcSpan(span)) {
            String resource = span.resource == null ? "" : span.resource.trim();
            if (resource.isBlank()) {
                return null;
            }
            return dataSource + ":" + resource;
        }
        return null;
    }
}
