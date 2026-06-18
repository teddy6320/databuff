package com.databuff.apm.ingest.receiver;

import com.databuff.apm.ingest.otel.OtlpIngestService;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OtlpHttpControllerTest {

    @Mock
    private OtlpIngestService ingestService;

    @InjectMocks
    private OtlpHttpController controller;

    @Test
    void acceptsProtobufTraces() throws Exception {
        ResponseEntity<Void> response = controller.traces(ExportTraceServiceRequest.getDefaultInstance().toByteArray());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ingestService).ingestTraces(any());
    }

    @Test
    void acceptsProtobufMetrics() throws Exception {
        ResponseEntity<Void> response = controller.metrics(
                io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest.getDefaultInstance()
                        .toByteArray());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ingestService).ingestMetrics(any());
    }
}
