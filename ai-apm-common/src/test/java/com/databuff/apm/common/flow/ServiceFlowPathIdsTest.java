package com.databuff.apm.common.flow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceFlowPathIdsTest {

    @Test
    void entryPathIdIsStable() {
        String first = ServiceFlowPathIds.entryPathId("abc123");
        String second = ServiceFlowPathIds.entryPathId("abc123");
        assertThat(first).isEqualTo(second);
        assertThat(first).matches("-?\\d+");
    }

    @Test
    void pathIdUsesParentLevelAndService() {
        String parent = ServiceFlowPathIds.entryPathId("svc");
        String child = ServiceFlowPathIds.pathId(parent, 1, "child");
        assertThat(child).isNotEqualTo(parent);
    }
}
