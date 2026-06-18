package com.databuff.apm.common.cluster.coordination;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterTaskSharderTest {

    @Test
    void splitsFourRulesAcrossTwoMembers() {
        List<String> members = List.of("web-1", "web-2");
        List<String> ownedByFirst = List.of("1", "2", "3", "4").stream()
                .filter(id -> ClusterTaskSharder.owns(id, "web-1", members))
                .toList();
        List<String> ownedBySecond = List.of("1", "2", "3", "4").stream()
                .filter(id -> ClusterTaskSharder.owns(id, "web-2", members))
                .toList();

        assertThat(ownedByFirst).hasSize(2);
        assertThat(ownedBySecond).hasSize(2);
        assertThat(ownedByFirst).doesNotContainAnyElementsOf(ownedBySecond);
    }

    @Test
    void singleMemberOwnsEverything() {
        List<String> members = List.of("web-1");
        assertThat(ClusterTaskSharder.owns("rule-1", "web-1", members)).isTrue();
        assertThat(ClusterTaskSharder.filterOwned(
                List.of("a", "b"),
                item -> item,
                "web-1",
                members)).containsExactly("a", "b");
    }

    @Test
    void sortedMembersUsesStableOrder() {
        assertThat(ClusterTaskSharder.sortedMembers(Map.of(
                "web-2", "h2",
                "web-1", "h1")))
                .containsExactly("web-1", "web-2");
    }
}
