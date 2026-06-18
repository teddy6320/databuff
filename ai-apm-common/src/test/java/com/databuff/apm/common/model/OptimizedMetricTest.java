package com.databuff.apm.common.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptimizedMetricTest {

    @Test
    void mergeRejectsDifferentFieldSizes() {
        OptimizedMetric left = new OptimizedMetric().withFieldValues(1);
        OptimizedMetric right = new OptimizedMetric().withFieldValues(1, 2);
        assertThatThrownBy(() -> left.merge(right))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
