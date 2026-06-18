package com.databuff.apm.ingest.config;

import com.databuff.apm.common.cluster.aggregate.ClusterPartialForwarder;
import com.databuff.apm.common.cluster.cache.ClusterCacheRegistry;
import com.databuff.apm.common.cluster.coordination.ClusterAdvertiseEndpoint;
import com.databuff.apm.common.cluster.coordination.ClusterInstanceCoordinator;
import com.databuff.apm.ingest.cluster.IngestNodeIds;
import com.databuff.apm.ingest.cluster.LeaderClusterCacheExecutor;
import com.databuff.apm.ingest.cluster.ClusterCoordinationGrpcServer;
import com.databuff.apm.ingest.cluster.ClusterCoordinationGrpcService;
import com.databuff.apm.ingest.cluster.GrpcClusterPartialForwarder;
import com.databuff.apm.ingest.cluster.GrpcLeaderClusterCacheTransport;
import com.databuff.apm.ingest.component.AggregateComponent;
import com.databuff.apm.ingest.component.TraceComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ClusterCoordinationConfiguration {

    @Bean(destroyMethod = "close")
    ClusterInstanceCoordinator clusterInstanceCoordinator(
            @Value("${ingest.node-id:ingest-1}") String nodeId,
            @Value("${HOSTNAME:}") String hostname,
            @Value("${POD_IP:}") String podIp,
            @Value("${ingest.cluster.grpc-port:18112}") int grpcPort,
            @Value("${ingest.zookeeper.connect-string:}") String zkConnectString,
            @Value("${ingest.cluster.enabled:false}") boolean clusterEnabled) {
        return ClusterInstanceCoordinator.create(new ClusterInstanceCoordinator.ClusterInstanceSettings(
                "ingest",
                IngestNodeIds.resolve(nodeId, hostname),
                ClusterAdvertiseEndpoint.resolve(podIp, grpcPort),
                zkConnectString,
                clusterEnabled));
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "ingest.cluster.enabled", havingValue = "true")
    ClusterPartialForwarder grpcClusterPartialForwarder(ClusterInstanceCoordinator coordinator) {
        return new GrpcClusterPartialForwarder(coordinator);
    }

    @Bean
    @ConditionalOnProperty(name = "ingest.cluster.enabled", havingValue = "false", matchIfMissing = true)
    ClusterPartialForwarder noopClusterPartialForwarder() {
        return ClusterPartialForwarder.NOOP;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "ingest.cluster.enabled", havingValue = "true")
    LeaderClusterCacheExecutor leaderClusterCacheExecutor(ClusterCacheRegistry clusterCacheRegistry) {
        return new LeaderClusterCacheExecutor(clusterCacheRegistry);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "ingest.cluster.enabled", havingValue = "true")
    GrpcLeaderClusterCacheTransport grpcLeaderClusterCacheTransport(
            ClusterInstanceCoordinator coordinator,
            ClusterCacheRegistry clusterCacheRegistry) {
        GrpcLeaderClusterCacheTransport transport = new GrpcLeaderClusterCacheTransport(coordinator);
        clusterCacheRegistry.setTransport(transport);
        return transport;
    }

    @Bean
    @ConditionalOnProperty(name = "ingest.cluster.enabled", havingValue = "true")
    ClusterCoordinationGrpcService clusterCoordinationGrpcService(
            AggregateComponent aggregateComponent,
            TraceComponent traceComponent,
            LeaderClusterCacheExecutor leaderClusterCacheExecutor,
            ClusterInstanceCoordinator coordinator) {
        return new ClusterCoordinationGrpcService(
                aggregateComponent, traceComponent, leaderClusterCacheExecutor, coordinator);
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    @ConditionalOnProperty(name = "ingest.cluster.enabled", havingValue = "true")
    ClusterCoordinationGrpcServer clusterCoordinationGrpcServer(
            ClusterCoordinationGrpcService service,
            @Value("${ingest.cluster.grpc-port:18112}") int grpcPort) throws IOException {
        return new ClusterCoordinationGrpcServer(grpcPort, service);
    }
}
