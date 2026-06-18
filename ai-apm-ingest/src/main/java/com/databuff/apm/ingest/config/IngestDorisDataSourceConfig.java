package com.databuff.apm.ingest.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.DorisConnectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class IngestDorisDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(IngestDorisDataSourceConfig.class);

    @Bean(destroyMethod = "close")
    DataSource ingestDorisDataSource(
            DorisConnectionConfig config,
            @Value("${ingest.doris.username:root}") String username,
            @Value("${ingest.doris.password:}") String password) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(config.feJdbcUrl());
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setQueryTimeout(60);
        dataSource.setValidationQuery("show status;");
        dataSource.setMaxActive(4);
        dataSource.setInitialSize(1);
        dataSource.setMaxWait(30_000);
        dataSource.setMinIdle(1);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        try {
            dataSource.init();
            log.info("Ingest Doris JDBC pool ready for {}:{}", config.feHost(), config.queryPort());
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ingest Doris JDBC pool init failed for " + config.feHost() + ":" + config.queryPort(), e);
        }
        return dataSource;
    }

    @Bean
    ApmReadRepository ingestApmReadRepository(DataSource ingestDorisDataSource) {
        return new ApmReadRepository(ingestDorisDataSource);
    }
}
