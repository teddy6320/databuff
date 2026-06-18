package com.databuff.apm.web.config;

import com.databuff.apm.common.cluster.coordination.ClusterAdvertiseEndpoint;
import com.databuff.apm.common.cluster.coordination.ClusterInstanceCoordinator;
import com.databuff.apm.web.cluster.WebNodeIds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClusterConfiguration {

    @Bean(destroyMethod = "close")
    ClusterInstanceCoordinator webClusterInstanceCoordinator(
            @Value("${apm.cluster.node-id:web-1}") String nodeId,
            @Value("${HOSTNAME:}") String hostname,
            @Value("${POD_IP:}") String podIp,
            @Value("${server.port:27403}") int serverPort,
            @Value("${apm.cluster.zookeeper.connect-string:}") String zkConnectString,
            @Value("${apm.cluster.enabled:false}") boolean clusterEnabled) {
        return ClusterInstanceCoordinator.create(new ClusterInstanceCoordinator.ClusterInstanceSettings(
                "web",
                WebNodeIds.resolve(nodeId, hostname),
                ClusterAdvertiseEndpoint.resolve(podIp, serverPort),
                zkConnectString,
                clusterEnabled));
    }
}
