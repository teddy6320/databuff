package com.databuff.apm.ingest.cluster;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class ClusterCoordinationGrpcServer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ClusterCoordinationGrpcServer.class);

    private final Server server;

    public ClusterCoordinationGrpcServer(int port, ClusterCoordinationGrpcService service) throws IOException {
        this.server = ServerBuilder.forPort(port).addService(service).build();
    }

    public void start() throws IOException {
        server.start();
        log.info("ClusterCoordination gRPC listening on port {}", server.getPort());
    }

    @Override
    public void close() {
        if (server != null) {
            server.shutdown();
            try {
                if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
