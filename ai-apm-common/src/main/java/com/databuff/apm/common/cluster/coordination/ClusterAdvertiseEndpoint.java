package com.databuff.apm.common.cluster.coordination;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Builds the host:port endpoint that a cluster node registers in ZooKeeper.
 */
public final class ClusterAdvertiseEndpoint {

    private ClusterAdvertiseEndpoint() {
    }

    public static String resolve(String podIp, int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("invalid cluster advertise port: " + port);
        }
        String host = podIp == null ? "" : podIp.trim();
        if (host.isEmpty()) {
            host = localHostAddress();
        }
        return host + ":" + port;
    }

    private static String localHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}
