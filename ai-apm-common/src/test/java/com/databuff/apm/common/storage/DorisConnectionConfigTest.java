package com.databuff.apm.common.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DorisConnectionConfigTest {

    @Test
    void feJdbcUrl() {
        DorisConnectionConfig cfg = new DorisConnectionConfig("doris-fe", 9030, 8030);
        assertThat(cfg.jdbcUrl("databuff")).contains("doris-fe:9030/databuff");
        assertThat(cfg.feJdbcUrl()).contains("doris-fe:9030/");
        assertThat(cfg.feHost()).isEqualTo("doris-fe");
        assertThat(cfg.queryPort()).isEqualTo(9030);
        assertThat(cfg.httpPort()).isEqualTo(8030);
    }

    @Test
    void fromEnvUsesDefaults() {
        DorisConnectionConfig cfg = DorisConnectionConfig.fromEnv();
        assertThat(cfg.feHost()).isNotBlank();
        assertThat(cfg.queryPort()).isPositive();
        assertThat(cfg.httpPort()).isPositive();
    }
}
