package com.databuff.apm.web.monitor;

import com.databuff.apm.web.TestStorageSupport;

import com.databuff.apm.common.query.ApmQueryModels.ErrorRateSnapshot;

import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThresholdEvaluationServiceTest {

    @Test
    void readsErrorRateFromDoris() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryErrorRate(anyString())).thenReturn(new ErrorRateSnapshot(1, 4));
        ThresholdEvaluationService service = new ThresholdEvaluationService(reader, TestStorageSupport.storage());
        assertThat(service.currentErrorRate("demo", 60_000)).isEqualTo(0.25);
    }

    @Test
    void errorRateBetweenUsesWindow() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryErrorRate(anyString())).thenReturn(new ErrorRateSnapshot(1, 10));
        ThresholdEvaluationService service = new ThresholdEvaluationService(reader, TestStorageSupport.storage());
        assertThat(service.errorRateBetween("demo", 0, 1000)).isEqualTo(0.1);
    }

    @Test
    void returnsZeroWhenQueryFails() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryErrorRate(anyString())).thenThrow(new RuntimeException("down"));
        ThresholdEvaluationService service = new ThresholdEvaluationService(reader, TestStorageSupport.storage());
        assertThat(service.currentErrorRate("demo", 60_000)).isZero();
    }

    @Test
    void readsRequestCountFromStore() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryRequestCount(anyString())).thenReturn(42L);
        ThresholdEvaluationService service = new ThresholdEvaluationService(reader, TestStorageSupport.storage());
        assertThat(service.requestCountBetween("demo", 0, 60_000)).isEqualTo(42);
    }

    @Test
    void returnsZeroRequestCountWhenQueryFails() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.queryRequestCount(anyString())).thenThrow(new RuntimeException("down"));
        ThresholdEvaluationService service = new ThresholdEvaluationService(reader, TestStorageSupport.storage());
        assertThat(service.requestCountBetween("demo", 0, 60_000)).isZero();
    }
}
