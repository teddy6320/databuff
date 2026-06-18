package com.databuff.apm.common.cluster.leadership;

import java.util.Optional;

/**
 * Role-scoped leader election (e.g. {@code web}, {@code ingest}).
 */
public interface ClusterLeadership extends AutoCloseable {

    String role();

    String localNodeId();

    boolean isLeader();

    Optional<String> leaderNodeId();
}
