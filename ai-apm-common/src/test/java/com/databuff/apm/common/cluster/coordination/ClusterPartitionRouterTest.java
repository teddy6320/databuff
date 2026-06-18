package com.databuff.apm.common.cluster.coordination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClusterPartitionRouterTest {

    @Test
    void stableOwner() {
        List<String> members = List.of("a", "b", "c");
        String k = "session-1";
        assertThat(ClusterPartitionRouter.chooseOwner(k, members))
                .isEqualTo(ClusterPartitionRouter.chooseOwner(k, members));
    }

    @Test
    void distributesAcrossMembers() {
        List<String> members = List.of("n1", "n2", "n3");
        long distinct = java.util.stream.LongStream.range(0, 100)
                .mapToObj(i -> ClusterPartitionRouter.chooseOwner("key-" + i, members))
                .distinct()
                .count();
        assertThat(distinct).isGreaterThan(1);
    }

    @Test
    void rejectsInvalidMemberCount() {
        assertThatThrownBy(() -> ClusterPartitionRouter.chooseOwnerIndex("k", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyMembers() {
        assertThatThrownBy(() -> ClusterPartitionRouter.chooseOwner("k", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
