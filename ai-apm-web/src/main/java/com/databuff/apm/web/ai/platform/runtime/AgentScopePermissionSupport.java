package com.databuff.apm.web.ai.platform.runtime;

import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;

/**
 * AgentScope 2.x requires explicit user confirmation for non-read-only MCP tools unless
 * permission mode bypasses the ASK flow. Our chat UI does not collect confirmations yet.
 */
public final class AgentScopePermissionSupport {

    private AgentScopePermissionSupport() {
    }

    public static PermissionContextState autoAllowContext() {
        return PermissionContextState.builder()
                .mode(PermissionMode.BYPASS)
                .build();
    }
}
