package com.databuff.apm.web.ai.platform.expert;

/** How an expert selects tools from the platform catalog. */
public enum ExpertToolAccessMode {
    /** Only {@code toolIds} are registered for this expert runtime. */
    ALLOWLIST,
    /** All enabled platform tools except those listed in {@code toolIds}. */
    BLOCKLIST
}
