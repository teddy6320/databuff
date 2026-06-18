package com.databuff.apm.ingest.support;

import com.databuff.apm.common.cluster.coordination.ClusterPartitionMembership;
import com.databuff.apm.common.cluster.coordination.ClusterTaskSharder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Test-only cluster membership fixtures for ingest module tests. */
public final class TestClusterMembership {

    private TestClusterMembership() {
    }

    public static ClusterPartitionMembership standalone(String localNodeId) {
        return new ClusterPartitionMembership() {
            @Override
            public String localNodeId() {
                return localNodeId;
            }

            @Override
            public List<String> sortedMembers() {
                return List.of(localNodeId);
            }

            @Override
            public Map<String, String> endpointsByNodeId() {
                return Map.of();
            }

            @Override
            public boolean effectiveClusterEnabled() {
                return false;
            }
        };
    }

    public static Mutable mutable(String localNodeId) {
        return new Mutable(localNodeId);
    }

    public static final class Mutable implements ClusterPartitionMembership {

        private final String localNodeId;
        private volatile boolean clusterEnabled;
        private final List<String> members = new ArrayList<>();
        private final Map<String, String> endpointsByNodeId = new ConcurrentHashMap<>();

        Mutable(String localNodeId) {
            this.localNodeId = localNodeId;
            members.add(localNodeId);
        }

        public void setClusterEnabled(boolean clusterEnabled) {
            this.clusterEnabled = clusterEnabled;
        }

        public void setMembers(List<String> memberIds) {
            members.clear();
            members.addAll(memberIds);
        }

        public void setEndpoint(String nodeId, String endpoint) {
            if (endpoint == null || endpoint.isBlank()) {
                endpointsByNodeId.remove(nodeId);
            } else {
                endpointsByNodeId.put(nodeId, endpoint);
            }
        }

        @Override
        public String localNodeId() {
            return localNodeId;
        }

        @Override
        public List<String> sortedMembers() {
            return ClusterTaskSharder.sortedMembers(members);
        }

        @Override
        public Map<String, String> endpointsByNodeId() {
            return Map.copyOf(endpointsByNodeId);
        }

        @Override
        public boolean effectiveClusterEnabled() {
            return clusterEnabled && sortedMembers().size() > 1;
        }
    }
}
