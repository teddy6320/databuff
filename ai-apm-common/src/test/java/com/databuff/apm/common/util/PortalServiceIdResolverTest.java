package com.databuff.apm.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalServiceIdResolverTest {

    @Test
    void normalizesReadableServiceNameToMd5Key() {
        assertThat(PortalServiceIdResolver.normalize("service-a")).isEqualTo("9bf61532d56eb7b5");
        assertThat(PortalServiceIdResolver.normalize("service-b")).isEqualTo("5457a0119281bb98");
    }

    @Test
    void keepsExistingMd5Key() {
        assertThat(PortalServiceIdResolver.normalize("9bf61532d56eb7b5")).isEqualTo("9bf61532d56eb7b5");
    }

    @Test
    void resolvePrefersMetricServiceId() {
        assertThat(PortalServiceIdResolver.resolve("fedcba0987654321", "service-b", "service-b"))
                .isEqualTo("fedcba0987654321");
    }

    @Test
    void matchesNameAndId() {
        assertThat(PortalServiceIdResolver.matches("service-a", "9bf61532d56eb7b5")).isTrue();
        assertThat(PortalServiceIdResolver.matches("9bf61532d56eb7b5", "service-a")).isTrue();
    }
}
