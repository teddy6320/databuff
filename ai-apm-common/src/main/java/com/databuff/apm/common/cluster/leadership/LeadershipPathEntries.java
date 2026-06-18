package com.databuff.apm.common.cluster.leadership;

public final class LeadershipPathEntries {

    private static final String LEADERS_ROOT = "/databuff/apm/leaders";

    private LeadershipPathEntries() {
    }

    public static String leaderLatchPath(String role) {
        return LEADERS_ROOT + "/" + normalizeRole(role);
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "default";
        }
        return role.trim();
    }
}
