package com.databuff.apm.ingest.cluster;

import com.databuff.apm.common.cluster.coordination.ClusterPartitionMembership;
import com.databuff.apm.common.cluster.coordination.ClusterPartitionRouter;
import com.databuff.apm.common.model.OptimizedMetric;

import java.util.Arrays;
import java.util.List;

/** Structured one-line summaries for cluster metric aggregation diagnostics. */
public final class ClusterAggregationLog {

    private ClusterAggregationLog() {
    }

    public static String metricBrief(OptimizedMetric metric) {
        if (metric == null) {
            return "null";
        }
        long cnt = metric.fieldValues().length > 0 ? metric.fieldValues()[0] : 0L;
        long err = metric.fieldValues().length > 1 ? metric.fieldValues()[1] : 0L;
        return metric.measurement()
                + " tags=" + Arrays.toString(metric.tagValues())
                + " cnt=" + cnt
                + " err=" + err;
    }

    public static String partitionKeyBrief(String partitionKey) {
        if (partitionKey == null) {
            return "null";
        }
        String[] parts = partitionKey.split("\u0001", 6);
        if (parts.length <= 1) {
            return shorten(partitionKey, 120);
        }
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append('/').append(parts[i]);
        }
        return shorten(sb.toString(), 120);
    }

    public static String membershipBrief(ClusterPartitionMembership membership) {
        List<String> members = membership.sortedMembers();
        return "local="
                + membership.localNodeId()
                + " cluster="
                + membership.effectiveClusterEnabled()
                + " members="
                + members.size()
                + " ids="
                + members;
    }

    public static String ownerBrief(ClusterPartitionMembership membership, String partitionKey) {
        List<String> members = membership.sortedMembers();
        if (members.isEmpty()) {
            return membership.localNodeId();
        }
        return ClusterPartitionRouter.chooseOwner(partitionKey, members);
    }

    private static String shorten(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 3) + "...";
    }
}
