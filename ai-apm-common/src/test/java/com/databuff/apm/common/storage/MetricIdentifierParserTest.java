package com.databuff.apm.common.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricIdentifierParserTest {

    @Test
    void parsesPoolMetric() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("service.object.pool.size");
        assertThat(parsed.measurement()).isEqualTo("service.object.pool");
        assertThat(parsed.field()).isEqualTo("size");
    }

    @Test
    void parsesPoolGetMetric() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("service.http.connection.pool.get.waitTime");
        assertThat(parsed.measurement()).isEqualTo("service.http.connection.pool.get");
        assertThat(parsed.field()).isEqualTo("waitTime");
    }

    @Test
    void parsesExceptionMetric() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("service.exception.cnt");
        assertThat(parsed.measurement()).isEqualTo("service.exception");
        assertThat(parsed.field()).isEqualTo("cnt");
    }

    @Test
    void parsesJvmMetric() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("jvm.thread_count");
        assertThat(parsed.measurement()).isEqualTo("jvm");
        assertThat(parsed.field()).isEqualTo("thread_count");
    }

    @Test
    void parsesJvmGcMetric() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("jvm.gc.major_collection_count");
        assertThat(parsed.measurement()).isEqualTo("jvm.gc");
        assertThat(parsed.field()).isEqualTo("major_collection_count");
    }

    @Test
    void qualifiesJvmSubMeasurementColumns() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("jvm.memory.heap.used");
        assertThat(MetricIdentifierParser.toDorisFieldColumn(parsed)).isEqualTo("memory_heap_used");
        assertThat(MetricIdentifierParser.dorisTableName(parsed.measurement())).isEqualTo("metric_jvm");
    }

    @Test
    void mapsJvmRootCpuFieldsToUnderscoreColumns() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("jvm.cpu_load_process");
        assertThat(parsed.measurement()).isEqualTo("jvm");
        assertThat(parsed.field()).isEqualTo("cpu_load_process");
        assertThat(MetricIdentifierParser.toDorisFieldColumn(parsed)).isEqualTo("cpu_load_process");
    }

    @Test
    void acceptsLegacyJvmCpuIdentifiers() {
        MetricIdentifierParser.ParsedMetric parsed =
                MetricIdentifierParser.parse("jvm.cpu_load.process");
        assertThat(parsed.field()).isEqualTo("cpu_load_process");
        assertThat(MetricIdentifierParser.toDorisFieldColumn(parsed)).isEqualTo("cpu_load_process");
    }

    @Test
    void mapsMeasurementToDorisTable() {
        assertThat(MetricIdentifierParser.dorisTableName("service.http")).isEqualTo("metric_service_http");
        assertThat(MetricIdentifierParser.dorisTableName("service.rpc")).isEqualTo("metric_service_rpc");
        assertThat(MetricIdentifierParser.dorisTableName("service.db")).isEqualTo("metric_service_db");
        assertThat(MetricIdentifierParser.dorisTableName("jvm.gc")).isEqualTo("metric_jvm");
    }

    @Test
    void mapsTagNames() {
        assertThat(MetricIdentifierParser.toColumnName("serviceId")).isEqualTo("service_id");
        assertThat(MetricIdentifierParser.toColumnName("url")).isEqualTo("url");
    }
}
