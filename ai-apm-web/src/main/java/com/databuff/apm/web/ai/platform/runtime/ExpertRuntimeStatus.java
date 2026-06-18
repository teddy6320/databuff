package com.databuff.apm.web.ai.platform.runtime;

import java.time.Instant;

public record ExpertRuntimeStatus(
        String expertId,
        long expertVersion,
        String cacheKey,
        boolean loaded,
        Instant loadedAt,
        String invalidationReason) {

    public static ExpertRuntimeStatus loaded(
            String expertId,
            long expertVersion,
            String cacheKey,
            Instant loadedAt) {
        return new ExpertRuntimeStatus(expertId, expertVersion, cacheKey, true, loadedAt, null);
    }

    public static ExpertRuntimeStatus notLoaded(String expertId) {
        return new ExpertRuntimeStatus(expertId, 0L, null, false, null, null);
    }
}
