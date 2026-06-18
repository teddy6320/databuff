package com.databuff.apm.web.persistence;

import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.monitor.AlarmSilenceStore;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlarmSilencePersistenceTest {

    @Test
    void skipsWhenSchemaNotReady() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.connection()).thenThrow(new SQLException("down"));
        AlarmSilencePersistence sync = new AlarmSilencePersistence(reader, new AlarmSilenceStore(), TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isFalse();
    }

    @Test
    void hydratesAndPersists() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet schemaRs = mock(ResultSet.class);
        ResultSet rowsRs = mock(ResultSet.class);
        when(reader.connection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(contains("config_alarm_silence"))).thenReturn(ps);
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("LIMIT 1")) {
                return schemaRs;
            }
            return rowsRs;
        });
        when(schemaRs.next()).thenReturn(true);
        Instant until = Instant.now().plusSeconds(3600);
        when(rowsRs.next()).thenReturn(true, false);
        when(rowsRs.getString("service")).thenReturn("demo-order");
        when(rowsRs.getTimestamp("silenced_until")).thenReturn(Timestamp.from(until));
        when(rowsRs.getTimestamp("updated_at")).thenReturn(Timestamp.from(Instant.now()));

        AlarmSilenceStore store = new AlarmSilenceStore();
        AlarmSilencePersistence sync = new AlarmSilencePersistence(reader, store, TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isTrue();
        assertThat(store.isSilenced("demo-order")).isTrue();

        sync.persistSilence("demo-order", until);
        doThrow(new SQLException("fail")).when(ps).executeUpdate();
        sync.persistSilence("other", until);
    }

    @Test
    void toleratesLoadFailure() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet schemaRs = mock(ResultSet.class);
        when(reader.connection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("LIMIT 1")) {
                return schemaRs;
            }
            throw new SQLException("load failed");
        });
        when(schemaRs.next()).thenReturn(true);

        AlarmSilencePersistence sync = new AlarmSilencePersistence(reader, new AlarmSilenceStore(), TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isFalse();
    }
}
