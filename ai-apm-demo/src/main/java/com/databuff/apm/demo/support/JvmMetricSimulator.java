package com.databuff.apm.demo.support;

import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.proto.resource.v1.Resource;

import java.time.Instant;

/**
 * Generates deterministic OTLP JVM metrics for demo/integration tests.
 * GC count/time fields advance by fixed deltas each export (cumulative counters).
 */
public final class JvmMetricSimulator {

    private static final long MIB = 1024L * 1024L;
    private static final DemoJvmProfile SERVICE_A = DemoJvmProfile.serviceA();
    private static final DemoJvmProfile SERVICE_B = DemoJvmProfile.serviceB();

    private JvmMetricSimulator() {
    }

    public static byte[] nextDemoExport() {
        long timeNanos = Instant.now().toEpochMilli() * 1_000_000L;
        return ExportMetricsServiceRequest.newBuilder()
                .addResourceMetrics(buildResourceMetrics(
                        OtlpTraceFixture.SERVICE_A, "service-a-1", "demo-host-a", SERVICE_A, timeNanos))
                .addResourceMetrics(buildResourceMetrics(
                        OtlpTraceFixture.SERVICE_B, "service-b-1", "demo-host-b", SERVICE_B, timeNanos))
                .build()
                .toByteArray();
    }

    public static byte[] gcOnlyExport() {
        long timeNanos = Instant.now().toEpochMilli() * 1_000_000L;
        SERVICE_A.advanceGcCounters();
        return ExportMetricsServiceRequest.newBuilder()
                .addResourceMetrics(ResourceMetrics.newBuilder()
                        .setResource(serviceResource(OtlpTraceFixture.DEMO_SERVICE, "service-a-1", "demo-host-a"))
                        .addScopeMetrics(ScopeMetrics.newBuilder()
                                .addMetrics(intGauge("jvm.gc.minor_collection_count", timeNanos,
                                        SERVICE_A.minorCollectionCount()))
                                .addMetrics(doubleGauge("jvm.gc.major_collection_time", timeNanos,
                                        SERVICE_A.majorCollectionTimeSec()))))
                .build()
                .toByteArray();
    }

    private static ResourceMetrics buildResourceMetrics(
            String service,
            String instanceId,
            String hostName,
            DemoJvmProfile profile,
            long timeNanos) {
        profile.advanceGcCounters();

        ScopeMetrics.Builder scope = ScopeMetrics.newBuilder()
                .addMetrics(intGauge("jvm.thread_count", timeNanos, profile.threadCount()))
                .addMetrics(doubleGauge("jvm.cpu_load_process", timeNanos, profile.cpuLoadProcess()))
                .addMetrics(doubleGauge("jvm.cpu_load_system", timeNanos, profile.cpuLoadSystem()))
                .addMetrics(intGauge("jvm.gc.minor_collection_count", timeNanos, profile.minorCollectionCount()))
                .addMetrics(intGauge("jvm.gc.major_collection_count", timeNanos, profile.majorCollectionCount()))
                .addMetrics(doubleGauge("jvm.gc.minor_collection_time", timeNanos, profile.minorCollectionTimeSec()))
                .addMetrics(doubleGauge("jvm.gc.major_collection_time", timeNanos, profile.majorCollectionTimeSec()))
                .addMetrics(longGauge("jvm.gc.eden_size", timeNanos, profile.edenSize()))
                .addMetrics(longGauge("jvm.gc.survivor_size", timeNanos, profile.survivorSize()))
                .addMetrics(longGauge("jvm.gc.old_gen_size", timeNanos, profile.oldGenSize()))
                .addMetrics(longGauge("jvm.gc.metaspace_size", timeNanos, profile.metaspaceSize()))
                .addMetrics(intGauge("jvm.loaded_classes.count", timeNanos, profile.loadedClasses()))
                .addMetrics(longGauge("jvm.memory.heap.init", timeNanos, profile.heapInit()))
                .addMetrics(longGauge("jvm.memory.heap.max", timeNanos, profile.heapMax()))
                .addMetrics(longGauge("jvm.memory.heap.committed", timeNanos, profile.heapCommitted()))
                .addMetrics(longGauge("jvm.memory.heap.used", timeNanos, profile.heapUsed()))
                .addMetrics(longGauge("jvm.memory.heap.free", timeNanos, profile.heapFree()))
                .addMetrics(doubleGauge("jvm.memory.heap.pct", timeNanos, profile.heapPct()))
                .addMetrics(longGauge("jvm.memory.noheap.init", timeNanos, profile.noHeapInit()))
                .addMetrics(longGauge("jvm.memory.noheap.max", timeNanos, profile.noHeapMax()))
                .addMetrics(longGauge("jvm.memory.noheap.committed", timeNanos, profile.noHeapCommitted()))
                .addMetrics(longGauge("jvm.memory.noheap.used", timeNanos, profile.noHeapUsed()))
                .addMetrics(longGauge("jvm.buffer_pool.direct.capacity", timeNanos, profile.directCapacity()))
                .addMetrics(intGauge("jvm.buffer_pool.direct.count", timeNanos, profile.directCount()))
                .addMetrics(longGauge("jvm.buffer_pool.direct.used", timeNanos, profile.directUsed()))
                .addMetrics(longGauge("jvm.buffer_pool.mapped.capacity", timeNanos, profile.mappedCapacity()))
                .addMetrics(intGauge("jvm.buffer_pool.mapped.count", timeNanos, profile.mappedCount()))
                .addMetrics(longGauge("jvm.buffer_pool.mapped.used", timeNanos, profile.mappedUsed()))
                .addMetrics(intGauge("service.thread.pool.poolSize", timeNanos, profile.threadPoolSize(),
                        kv("thread.pool.name", "common-pool")))
                .addMetrics(intGauge("service.thread.pool.maximumPoolSize", timeNanos, 20,
                        kv("thread.pool.name", "common-pool")))
                .addMetrics(intGauge("service.object.pool.maxSize", timeNanos, 50,
                        kv("object.pool.name", "demo-object-pool")))
                .addMetrics(intGauge("service.object.pool.size", timeNanos, profile.objectPoolSize(),
                        kv("object.pool.name", "demo-object-pool")))
                .addMetrics(intGauge("service.http.connection.pool.maxSize", timeNanos, 100,
                        kv("http.connection.pool.name", "demo-http-pool")))
                .addMetrics(intGauge("service.http.connection.pool.size", timeNanos, profile.httpPoolSize(),
                        kv("http.connection.pool.name", "demo-http-pool")))
                .addMetrics(intGauge("service.db.connection.pool.maxSize", timeNanos, 30,
                        kv("db.connection.pool.name", "demo-db-pool")))
                .addMetrics(intGauge("service.db.connection.pool.size", timeNanos, profile.dbPoolSize(),
                        kv("db.connection.pool.name", "demo-db-pool")));

        return ResourceMetrics.newBuilder()
                .setResource(serviceResource(service, instanceId, hostName))
                .addScopeMetrics(scope)
                .build();
    }

    private static Resource.Builder serviceResource(String serviceName, String instanceId, String hostName) {
        return Resource.newBuilder()
                .addAttributes(kv("service.name", serviceName))
                .addAttributes(kv("host.name", hostName))
                .addAttributes(kv("service.instance.id", instanceId))
                .addAttributes(kv("k8s.namespace.name", "demo"));
    }

    private static Metric intGauge(String name, long timeNanos, long value, KeyValue... attributes) {
        return numberGauge(name, timeNanos, value, attributes);
    }

    private static Metric longGauge(String name, long timeNanos, long value, KeyValue... attributes) {
        return numberGauge(name, timeNanos, value, attributes);
    }

    private static Metric doubleGauge(String name, long timeNanos, double value, KeyValue... attributes) {
        NumberDataPoint.Builder point = NumberDataPoint.newBuilder()
                .setTimeUnixNano(timeNanos)
                .setAsDouble(value);
        for (KeyValue attribute : attributes) {
            point.addAttributes(attribute);
        }
        return Metric.newBuilder()
                .setName(name)
                .setGauge(Gauge.newBuilder().addDataPoints(point))
                .build();
    }

    private static Metric numberGauge(String name, long timeNanos, long value, KeyValue... attributes) {
        NumberDataPoint.Builder point = NumberDataPoint.newBuilder()
                .setTimeUnixNano(timeNanos)
                .setAsInt(value);
        for (KeyValue attribute : attributes) {
            point.addAttributes(attribute);
        }
        return Metric.newBuilder()
                .setName(name)
                .setGauge(Gauge.newBuilder().addDataPoints(point))
                .build();
    }

    private static KeyValue kv(String key, String value) {
        return KeyValue.newBuilder()
                .setKey(key)
                .setValue(AnyValue.newBuilder().setStringValue(value))
                .build();
    }

    private static final class DemoJvmProfile {
        private final int threadCount;
        private final double cpuLoadProcess;
        private final double cpuLoadSystem;
        private final long edenSize;
        private final long survivorSize;
        private final long oldGenSize;
        private final long metaspaceSize;
        private final int loadedClasses;
        private final long heapInit;
        private final long heapMax;
        private final long heapCommitted;
        private final long heapUsed;
        private final long heapFree;
        private final double heapPct;
        private final long noHeapInit;
        private final long noHeapMax;
        private final long noHeapCommitted;
        private final long noHeapUsed;
        private final long directCapacity;
        private final int directCount;
        private final long directUsed;
        private final long mappedCapacity;
        private final int mappedCount;
        private final long mappedUsed;
        private final int threadPoolSize;
        private final int objectPoolSize;
        private final int httpPoolSize;
        private final int dbPoolSize;
        private final long initialMinorCollectionCount;
        private final long initialMajorCollectionCount;
        private final double initialMinorCollectionTimeSec;
        private final double initialMajorCollectionTimeSec;
        private final int minorGcDelta;
        private final int majorGcDelta;
        private final double minorGcTimeDeltaSec;
        private final double majorGcTimeDeltaSec;

        private long minorCollectionCount;
        private long majorCollectionCount;
        private double minorCollectionTimeSec;
        private double majorCollectionTimeSec;

        private DemoJvmProfile(
                int threadCount,
                double cpuLoadProcess,
                double cpuLoadSystem,
                long edenSize,
                long survivorSize,
                long oldGenSize,
                long metaspaceSize,
                int loadedClasses,
                long heapInit,
                long heapMax,
                long heapCommitted,
                long heapUsed,
                long noHeapInit,
                long noHeapMax,
                long noHeapCommitted,
                long noHeapUsed,
                long directCapacity,
                int directCount,
                long directUsed,
                long mappedCapacity,
                int mappedCount,
                long mappedUsed,
                int threadPoolSize,
                int objectPoolSize,
                int httpPoolSize,
                int dbPoolSize,
                long initialMinorCollectionCount,
                long initialMajorCollectionCount,
                double initialMinorCollectionTimeSec,
                double initialMajorCollectionTimeSec,
                int minorGcDelta,
                int majorGcDelta,
                double minorGcTimeDeltaSec,
                double majorGcTimeDeltaSec) {
            this.threadCount = threadCount;
            this.cpuLoadProcess = cpuLoadProcess;
            this.cpuLoadSystem = cpuLoadSystem;
            this.edenSize = edenSize;
            this.survivorSize = survivorSize;
            this.oldGenSize = oldGenSize;
            this.metaspaceSize = metaspaceSize;
            this.loadedClasses = loadedClasses;
            this.heapInit = heapInit;
            this.heapMax = heapMax;
            this.heapCommitted = heapCommitted;
            this.heapUsed = heapUsed;
            this.heapFree = Math.max(0, heapMax - heapUsed);
            this.heapPct = heapUsed * 100.0 / heapMax;
            this.noHeapInit = noHeapInit;
            this.noHeapMax = noHeapMax;
            this.noHeapCommitted = noHeapCommitted;
            this.noHeapUsed = noHeapUsed;
            this.directCapacity = directCapacity;
            this.directCount = directCount;
            this.directUsed = directUsed;
            this.mappedCapacity = mappedCapacity;
            this.mappedCount = mappedCount;
            this.mappedUsed = mappedUsed;
            this.threadPoolSize = threadPoolSize;
            this.objectPoolSize = objectPoolSize;
            this.httpPoolSize = httpPoolSize;
            this.dbPoolSize = dbPoolSize;
            this.initialMinorCollectionCount = initialMinorCollectionCount;
            this.initialMajorCollectionCount = initialMajorCollectionCount;
            this.initialMinorCollectionTimeSec = initialMinorCollectionTimeSec;
            this.initialMajorCollectionTimeSec = initialMajorCollectionTimeSec;
            this.minorGcDelta = minorGcDelta;
            this.majorGcDelta = majorGcDelta;
            this.minorGcTimeDeltaSec = minorGcTimeDeltaSec;
            this.majorGcTimeDeltaSec = majorGcTimeDeltaSec;
            this.minorCollectionCount = initialMinorCollectionCount;
            this.majorCollectionCount = initialMajorCollectionCount;
            this.minorCollectionTimeSec = initialMinorCollectionTimeSec;
            this.majorCollectionTimeSec = initialMajorCollectionTimeSec;
        }

        static DemoJvmProfile serviceA() {
            return new DemoJvmProfile(
                    28,
                    0.15,
                    0.35,
                    100 * MIB,
                    20 * MIB,
                    150 * MIB,
                    80 * MIB,
                    8456,
                    256 * MIB,
                    512 * MIB,
                    384 * MIB,
                    268435456L,
                    64 * MIB,
                    256 * MIB,
                    128 * MIB,
                    96 * MIB,
                    64 * MIB,
                    14,
                    20 * MIB,
                    32 * MIB,
                    5,
                    8 * MIB,
                    10,
                    12,
                    8,
                    5,
                    150,
                    8,
                    12.5,
                    3.2,
                    2,
                    0,
                    0.06,
                    0.0);
        }

        static DemoJvmProfile serviceB() {
            return new DemoJvmProfile(
                    24,
                    0.12,
                    0.30,
                    88 * MIB,
                    18 * MIB,
                    132 * MIB,
                    72 * MIB,
                    8312,
                    256 * MIB,
                    512 * MIB,
                    384 * MIB,
                    251658240L,
                    64 * MIB,
                    256 * MIB,
                    128 * MIB,
                    88 * MIB,
                    64 * MIB,
                    12,
                    18 * MIB,
                    32 * MIB,
                    4,
                    6 * MIB,
                    9,
                    11,
                    7,
                    4,
                    95,
                    5,
                    8.1,
                    2.0,
                    1,
                    0,
                    0.04,
                    0.0);
        }

        void advanceGcCounters() {
            minorCollectionCount += minorGcDelta;
            majorCollectionCount += majorGcDelta;
            minorCollectionTimeSec += minorGcTimeDeltaSec;
            majorCollectionTimeSec += majorGcTimeDeltaSec;
        }

        int threadCount() {
            return threadCount;
        }

        double cpuLoadProcess() {
            return cpuLoadProcess;
        }

        double cpuLoadSystem() {
            return cpuLoadSystem;
        }

        long minorCollectionCount() {
            return minorCollectionCount;
        }

        long majorCollectionCount() {
            return majorCollectionCount;
        }

        double minorCollectionTimeSec() {
            return minorCollectionTimeSec;
        }

        double majorCollectionTimeSec() {
            return majorCollectionTimeSec;
        }

        long edenSize() {
            return edenSize;
        }

        long survivorSize() {
            return survivorSize;
        }

        long oldGenSize() {
            return oldGenSize;
        }

        long metaspaceSize() {
            return metaspaceSize;
        }

        int loadedClasses() {
            return loadedClasses;
        }

        long heapInit() {
            return heapInit;
        }

        long heapMax() {
            return heapMax;
        }

        long heapCommitted() {
            return heapCommitted;
        }

        long heapUsed() {
            return heapUsed;
        }

        long heapFree() {
            return heapFree;
        }

        double heapPct() {
            return heapPct;
        }

        long noHeapInit() {
            return noHeapInit;
        }

        long noHeapMax() {
            return noHeapMax;
        }

        long noHeapCommitted() {
            return noHeapCommitted;
        }

        long noHeapUsed() {
            return noHeapUsed;
        }

        long directCapacity() {
            return directCapacity;
        }

        int directCount() {
            return directCount;
        }

        long directUsed() {
            return directUsed;
        }

        long mappedCapacity() {
            return mappedCapacity;
        }

        int mappedCount() {
            return mappedCount;
        }

        long mappedUsed() {
            return mappedUsed;
        }

        int threadPoolSize() {
            return threadPoolSize;
        }

        int objectPoolSize() {
            return objectPoolSize;
        }

        int httpPoolSize() {
            return httpPoolSize;
        }

        int dbPoolSize() {
            return dbPoolSize;
        }
    }
}
