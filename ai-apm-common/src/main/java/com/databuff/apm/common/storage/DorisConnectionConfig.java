package com.databuff.apm.common.storage;

public record DorisConnectionConfig(
        String feHost,
        int queryPort,
        int httpPort,
        String beHttpHost,
        int beHttpPort) {

    public DorisConnectionConfig(String feHost, int queryPort, int httpPort) {
        this(feHost, queryPort, httpPort, null, defaultBeHttpPort());
    }

    public static DorisConnectionConfig fromEnv() {
        String host = System.getenv().getOrDefault("DORIS_FE_HOST", "127.0.0.1");
        int query = Integer.parseInt(System.getenv().getOrDefault("DORIS_FE_QUERY_PORT", "9030"));
        int http = Integer.parseInt(System.getenv().getOrDefault("DORIS_FE_HTTP_PORT", "8030"));
        String beHost = System.getenv("DORIS_BE_HTTP_HOST");
        if (beHost != null && beHost.isBlank()) {
            beHost = null;
        }
        return new DorisConnectionConfig(host, query, http, beHost, defaultBeHttpPort());
    }

    /** Host used when rewriting FE→BE stream-load redirects (defaults to FE host). */
    public String effectiveBeHttpHost() {
        if (beHttpHost != null && !beHttpHost.isBlank()) {
            return beHttpHost;
        }
        return feHost;
    }

    /** Stream load from outside the cluster must rewrite internal BE addresses. */
    public boolean rewritesBeRedirect() {
        return true;
    }

    /** When BE HTTP host is configured, upload directly to BE (avoids FE 307 + HttpClient hang). */
    public boolean preferDirectBeStreamLoad() {
        return beHttpHost != null && !beHttpHost.isBlank();
    }

    public String feJdbcUrl() {
        return jdbcUrl("") + "&connectTimeout=5000&socketTimeout=30000";
    }

    public String jdbcUrl(String database) {
        return "jdbc:mysql://" + feHost + ":" + queryPort + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    private static int defaultBeHttpPort() {
        return Integer.parseInt(System.getenv().getOrDefault("DORIS_BE_HTTP_PORT", "8040"));
    }
}
