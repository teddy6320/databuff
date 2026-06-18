package com.databuff.apm.common.cluster.coordination;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class StaticClusterMemberDirectory implements ClusterMemberDirectory {

    private final String localNodeId;
    private final Map<String, String> endpointsByNodeId;

    public StaticClusterMemberDirectory(String localNodeId, Map<String, String> endpointsByNodeId) {
        this.localNodeId = Objects.requireNonNull(localNodeId, "localNodeId");
        this.endpointsByNodeId = Map.copyOf(endpointsByNodeId);
    }

    public static StaticClusterMemberDirectory local(String localNodeId, String localEndpoint) {
        if (localEndpoint == null || localEndpoint.isBlank()) {
            return new StaticClusterMemberDirectory(localNodeId, Collections.emptyMap());
        }
        Map<String, String> map = new LinkedHashMap<>();
        map.put(localNodeId, localEndpoint.trim());
        return new StaticClusterMemberDirectory(localNodeId, map);
    }

    @Override
    public String localNodeId() {
        return localNodeId;
    }

    @Override
    public Map<String, String> endpointsByNodeId() {
        return endpointsByNodeId;
    }
}
