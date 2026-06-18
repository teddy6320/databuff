package com.databuff.apm.common.cluster.coordination;

import org.apache.curator.framework.recipes.cache.ChildData;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ZkMemberEntries {

    static final String MEMBERS_ROOT = "/databuff/apm/members";
    /** Legacy ingest path kept for backward compatibility. */
    static final String LEGACY_INGEST_MEMBERS_ROOT = MEMBERS_ROOT;

    private ZkMemberEntries() {
    }

    static String membersRoot(String role) {
        if (role == null || role.isBlank() || "ingest".equals(role.trim())) {
            return LEGACY_INGEST_MEMBERS_ROOT;
        }
        return MEMBERS_ROOT + "/" + role.trim();
    }

    static String memberPath(String role, String nodeId) {
        return membersRoot(role) + "/" + nodeId;
    }

    static String memberPath(String nodeId) {
        return memberPath("ingest", nodeId);
    }

    static Map<String, String> fromChildren(List<ChildData> children) {
        if (children == null || children.isEmpty()) {
            return Map.of();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (ChildData child : children) {
            if (child == null || child.getPath() == null) {
                continue;
            }
            int slash = child.getPath().lastIndexOf('/');
            if (slash < 0 || slash == child.getPath().length() - 1) {
                continue;
            }
            String nodeId = child.getPath().substring(slash + 1).trim();
            if (nodeId.isEmpty()) {
                continue;
            }
            byte[] data = child.getData();
            String endpoint = data == null ? "" : new String(data, StandardCharsets.UTF_8).trim();
            if (!endpoint.isEmpty()) {
                map.put(nodeId, endpoint);
            }
        }
        return Map.copyOf(map);
    }
}
