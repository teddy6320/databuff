package com.databuff.apm.web.persistence;

import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.cockpit.TrafficLightService;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrafficLightConfigPersistenceTest {

    @Test
    void hydrateKeepsDefaultsWhenStoreUnavailable() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        TrafficLightService trafficLightService = new TrafficLightService(reader, TestStorageSupport.storage());
        TrafficLightConfigPersistence sync = new TrafficLightConfigPersistence(
                trafficLightService, reader, TestStorageSupport.storage());

        when(reader.connection()).thenAnswer(invocation -> {
            throw new java.sql.SQLException("down");
        });
        sync.reloadFromStore();
        assertThat(trafficLightService.getConfig()).containsEntry("errorRateThreshold", 0.05);
    }

    @Test
    void persistSkipsWhenNotEnabled() {
        TrafficLightService trafficLightService = new TrafficLightService(mock(ApmReadRepository.class), TestStorageSupport.storage());
        TrafficLightConfigPersistence sync = new TrafficLightConfigPersistence(
                trafficLightService, mock(ApmReadRepository.class), TestStorageSupport.storage());
        sync.persist(Map.of("errorRateThreshold", 0.1));
    }

    @Test
    void persistWritesWhenEnabled() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        java.sql.Statement statement = mock(java.sql.Statement.class);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet tableRs = mock(java.sql.ResultSet.class);
        java.sql.ResultSet configRs = mock(java.sql.ResultSet.class);
        when(reader.connection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(tableRs, configRs);
        when(tableRs.next()).thenReturn(true);
        when(configRs.next()).thenReturn(true, false);
        when(configRs.getString("config_key")).thenReturn("errorRateThreshold");
        when(configRs.getString("config_value")).thenReturn("0.07");
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        TrafficLightService trafficLightService = new TrafficLightService(reader, TestStorageSupport.storage());
        TrafficLightConfigPersistence sync = new TrafficLightConfigPersistence(
                trafficLightService, reader, TestStorageSupport.storage());
        sync.reloadFromStore();
        sync.persist(Map.of("errorRateThreshold", 0.08, "minRequestCount", 15));

        verify(ps, org.mockito.Mockito.atLeastOnce()).executeUpdate();
        assertThat(trafficLightService.getConfig().get("errorRateThreshold")).isEqualTo(0.07);
    }
}
