package com.databuff.apm.ingest.cluster;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngestNodeIdsTest {

    @Test
    void resolvesAutoFromStatefulSetName() {
        assertThat(IngestNodeIds.resolve("auto", "ai-apm-ingest-0")).isEqualTo("ingest-1");
        assertThat(IngestNodeIds.resolve("auto", "ai-apm-ingest-1")).isEqualTo("ingest-2");
    }

    @Test
    void keepsExplicitNodeId() {
        assertThat(IngestNodeIds.resolve("ingest-west", "ai-apm-ingest-0")).isEqualTo("ingest-west");
    }

    @Test
    void resolvesDeploymentStylePodName() {
        assertThat(IngestNodeIds.resolve("auto", "ai-apm-ingest-7d4f8b9c5-abcde"))
                .isEqualTo("ai-apm-ingest-7d4f8b9c5-abcde");
    }

    @Test
    void usesHostnameWhenOrdinalMissing() {
        assertThat(IngestNodeIds.resolve("auto", "noordinal")).isEqualTo("noordinal");
    }
}
