package com.databuff.apm.common.meta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualComponentNamingTest {

    @Test
    void buildsDbServiceWithInstanceAndHost() {
        VirtualComponentNaming.NamedInstance named = VirtualComponentNaming.resolve(
                "mysql", "orders", "10.0.0.8", "3306", "checkout");

        assertThat(named.service()).isEqualTo("[mysql]orders");
        assertThat(named.serviceInstance()).isEqualTo("10.0.0.8");
    }

    @Test
    void buildsHostPortServiceWhenDbInstanceMissing() {
        VirtualComponentNaming.NamedInstance named = VirtualComponentNaming.resolve(
                "redis", null, "cache-1", "6379", "checkout");

        assertThat(named.service()).isEqualTo("[redis]cache-1:6379");
        assertThat(named.serviceInstance()).isEqualTo("cache-1");
    }

    @Test
    void buildsMqServiceFromTopicAndBroker() {
        VirtualComponentNaming.NamedInstance named = VirtualComponentNaming.mq(
                "kafka", "order-events", "broker-1");

        assertThat(named.service()).isEqualTo("[kafka]order-events");
        assertThat(named.serviceInstance()).isEqualTo("broker-1");
    }
}
