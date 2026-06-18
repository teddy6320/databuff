package com.databuff.apm.ingest.trace.remote;

import com.databuff.apm.common.meta.OtelAttributeMaps;
import com.databuff.apm.common.model.DcSpan;
import com.databuff.apm.common.serde.DcSpanUtil;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

/**
 * Peer address extraction; legacy portal compatibility ({@code ServiceUtil#getRemotelySvc}).
 */
public final class RemotePeerAddressUtil {

    public record RemotePeer(String type, String hostname, String port) {
        public String remoteServiceKey() {
            return type + "-" + hostname + ":" + port;
        }
    }

    private RemotePeerAddressUtil() {
    }

    public static RemotePeer resolve(DcSpan span, Map<String, String> meta) {
        if (span == null) {
            return null;
        }
        Map<String, String> attrs = meta == null ? Map.of() : meta;
        String peerHostname = firstNonBlank(
                span.metaPeerHostname,
                OtelAttributeMaps.firstNonBlank(attrs, "peer.hostname", "server.address", "net.peer.name"));
        String peerPort = OtelAttributeMaps.firstNonBlank(
                attrs, "peer.port", "server.port", "net.peer.port");
        String type = resolveRemoteType(span, attrs);

        if (DcSpanUtil.isHttpSpan(span)) {
            String httpUrl = firstNonBlank(span.metaHttpUrl, OtelAttributeMaps.firstNonBlank(attrs, "http.url", "url.full"));
            if (peerHostname != null && (peerPort == null || peerPort.isBlank()) && httpUrl != null) {
                peerPort = defaultHttpPort(httpUrl);
            } else if (peerHostname == null && (peerPort == null || peerPort.isBlank()) && httpUrl != null) {
                try {
                    URI uri = new URI(httpUrl.contains("://") ? httpUrl : "http://" + httpUrl);
                    if (uri.getHost() != null) {
                        peerHostname = uri.getHost();
                    }
                    if (uri.getPort() != -1) {
                        peerPort = String.valueOf(uri.getPort());
                    } else {
                        peerPort = defaultHttpPort(httpUrl);
                    }
                } catch (Exception ignored) {
                    // keep partial values
                }
            }
        }

        if (peerHostname == null || peerHostname.isBlank()) {
            if (DcSpanUtil.isRpcSpan(span)) {
                String resource = span.resource == null ? "" : span.resource.trim();
                if (!resource.isBlank()) {
                    peerHostname = resource;
                    if (peerPort == null || peerPort.isBlank()) {
                        peerPort = "";
                    }
                }
            }
        }

        if (peerHostname == null || peerHostname.isBlank()) {
            return null;
        }
        peerHostname = peerHostname.trim();
        if ("localhost".equalsIgnoreCase(peerHostname) || "127.0.0.1".equals(peerHostname)) {
            return null;
        }
        if (peerPort == null) {
            peerPort = "";
        }
        return new RemotePeer(type, peerHostname, peerPort.trim());
    }

    private static String resolveRemoteType(DcSpan span, Map<String, String> meta) {
        if (DcSpanUtil.isHttpSpan(span)) {
            return "Http";
        }
        if (DcSpanUtil.isRpcSpan(span)) {
            String rpcSystem = OtelAttributeMaps.firstNonBlank(meta, "rpc.system");
            if (rpcSystem == null || rpcSystem.isBlank()) {
                return "Unknown";
            }
            String normalized = rpcSystem.trim().toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "dubbo" -> "Dubbo";
                case "grpc" -> "Grpc";
                case "sofa" -> "Sofarpc";
                default -> rpcSystem.trim();
            };
        }
        return "Unknown";
    }

    private static String defaultHttpPort(String httpUrl) {
        if (httpUrl == null) {
            return "80";
        }
        String lower = httpUrl.toLowerCase(Locale.ROOT);
        if (lower.startsWith("https")) {
            return "443";
        }
        return "80";
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
