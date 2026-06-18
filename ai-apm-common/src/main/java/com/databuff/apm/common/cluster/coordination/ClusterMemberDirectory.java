package com.databuff.apm.common.cluster.coordination;

import java.util.Map;

/**
 * Discovers cluster member gRPC endpoints (ingest or web).
 * Standalone uses {@link StaticClusterMemberDirectory}; cluster mode may use ZK-backed impl.
 */
public interface ClusterMemberDirectory {

    String localNodeId();

    Map<String, String> endpointsByNodeId();
}
