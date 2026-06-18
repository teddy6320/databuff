package com.databuff.apm.common.cluster.coordination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Deterministic task sharding across sorted cluster members. */
public final class ClusterTaskSharder {

    private ClusterTaskSharder() {
    }

    public static List<String> sortedMembers(Map<String, String> endpointsByNodeId) {
        if (endpointsByNodeId == null || endpointsByNodeId.isEmpty()) {
            return List.of();
        }
        List<String> members = new ArrayList<>(endpointsByNodeId.keySet());
        members.sort(String::compareTo);
        return List.copyOf(members);
    }

    public static List<String> sortedMembers(Collection<String> members) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        List<String> sorted = new ArrayList<>(members);
        sorted.sort(String::compareTo);
        return List.copyOf(sorted);
    }

    public static boolean owns(String itemKey, String localNodeId, List<String> sortedMembers) {
        if (itemKey == null || localNodeId == null) {
            return false;
        }
        if (sortedMembers == null || sortedMembers.isEmpty()) {
            return true;
        }
        if (sortedMembers.size() == 1) {
            return sortedMembers.get(0).equals(localNodeId);
        }
        int ownerIndex = ClusterPartitionRouter.chooseOwnerIndex(itemKey, sortedMembers.size());
        return sortedMembers.get(ownerIndex).equals(localNodeId);
    }

    public static <T> List<T> filterOwned(
            List<T> items,
            Function<T, String> keyExtractor,
            String localNodeId,
            List<String> sortedMembers) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(item -> owns(keyExtractor.apply(item), localNodeId, sortedMembers))
                .toList();
    }
}
