package com.databuff.apm.ingest.trace.remote;

import java.util.List;

/**
 * Remote service generation settings; legacy ingest pipeline compatibility ({@code RefreshScopeConfig}).
 */
public record RemoteServiceSettings(
        boolean enabled,
        long protectTimeMs,
        List<String> mergeServices) {

    public static final long DEFAULT_PROTECT_TIME_MS = 3_600_000L;

    public RemoteServiceSettings {
        mergeServices = mergeServices == null ? List.of() : List.copyOf(mergeServices);
    }

    public static RemoteServiceSettings defaults() {
        return new RemoteServiceSettings(true, DEFAULT_PROTECT_TIME_MS, List.of());
    }
}
