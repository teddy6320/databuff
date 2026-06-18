package com.databuff.apm.common.cluster.coordination;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public final class ClusterPartitionRouter {

    private ClusterPartitionRouter() {
    }

    public static int chooseOwnerIndex(String partitionKey, int memberCount) {
        if (memberCount <= 0) {
            throw new IllegalArgumentException("memberCount must be positive");
        }
        Objects.requireNonNull(partitionKey);
        int hash = murmur3(partitionKey);
        return Math.floorMod(hash, memberCount);
    }

    public static String chooseOwner(String partitionKey, List<String> members) {
        Objects.requireNonNull(members);
        if (members.isEmpty()) {
            throw new IllegalArgumentException("members must not be empty");
        }
        return members.get(chooseOwnerIndex(partitionKey, members.size()));
    }

    static int murmur3(String key) {
        byte[] data = key.getBytes(StandardCharsets.UTF_8);
        int h = 0;
        for (byte b : data) {
            h = 31 * h + (b & 0xff);
        }
        return h;
    }
}
