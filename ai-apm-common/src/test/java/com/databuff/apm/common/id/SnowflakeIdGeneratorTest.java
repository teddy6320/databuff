package com.databuff.apm.common.id;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakeIdGeneratorTest {

    @Test
    void generatesUniqueIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(generator.nextId());
        }
        assertThat(ids).hasSize(1000);
    }

    @Test
    void idsAreMonotonic() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(2);
        long previous = generator.nextId();
        for (int i = 0; i < 100; i++) {
            long current = generator.nextId();
            assertThat(current).isGreaterThan(previous);
            previous = current;
        }
    }
}
