package com.databuff.apm.common.cluster.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClusterAggregatorTest {

    @Test
    void mergesPartialsLocally() {
        ClusterAggregator agg = new ClusterAggregator("n1");
        agg.registerMerger("ingest.metric.optimized", (a, b) -> {
            int sum = Integer.parseInt(new String(a)) + Integer.parseInt(new String(b));
            return Integer.toString(sum).getBytes();
        });
        agg.emitStage1("ingest.metric.optimized", "svc1", 0, 60, "1".getBytes());
        agg.emitStage1("ingest.metric.optimized", "svc1", 0, 60, "2".getBytes());
        assertThat(new String(agg.getMergedState("ingest.metric.optimized", "svc1", 0, 60))).isEqualTo("3");
    }

    @Test
    void clearWindowRemovesState() {
        ClusterAggregator agg = new ClusterAggregator("n1");
        agg.registerMerger("s", (a, b) -> b);
        agg.emitStage1("s", "k", 0, 60, "x".getBytes());
        agg.clearWindow("s", "k", 0, 60);
        assertThat(agg.getMergedState("s", "k", 0, 60)).isNull();
    }

    @Test
    void rejectsUnknownStream() {
        ClusterAggregator agg = new ClusterAggregator("n1");
        assertThatThrownBy(() -> agg.emitStage1("missing", "k", 0, 60, new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getMergedStateReturnsNullForUnknownStream() {
        assertThat(new ClusterAggregator("n1").getMergedState("x", "k", 0, 60)).isNull();
    }
}
