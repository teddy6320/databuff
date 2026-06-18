package com.databuff.apm.web.persistence;

import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.monitor.Alarm;
import com.databuff.apm.web.monitor.AlarmStore;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.monitor.TestMonitorRecordIds;
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

class AlarmPersistenceTest {

    @Test
    void hydratesAndPersists() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet schemaRs = mock(ResultSet.class);
        ResultSet eventsRs = mock(ResultSet.class);
        when(reader.connection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(contains("config_alarm"))).thenReturn(ps);
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("LIMIT 1")) {
                return schemaRs;
            }
            return eventsRs;
        });
        when(schemaRs.next()).thenReturn(true);
        Instant now = Instant.now();
        when(eventsRs.next()).thenReturn(true, false);
        when(eventsRs.getLong("policy_id")).thenReturn(2L);
        when(eventsRs.getString("id")).thenReturn("A9");
        when(eventsRs.getString("service")).thenReturn("demo-order");
        when(eventsRs.getString("detection_way")).thenReturn(EventRule.WAY_THRESHOLD);
        when(eventsRs.getString("level")).thenReturn("warning");
        when(eventsRs.getString("message")).thenReturn("breached");
        when(eventsRs.getString("status")).thenReturn("open");
        when(eventsRs.getTimestamp("triggered_at")).thenReturn(Timestamp.from(now));
        when(eventsRs.getTimestamp("resolved_at")).thenReturn(null);

        AlarmStore store = new AlarmStore(TestMonitorRecordIds.create());
        AlarmPersistence sync = new AlarmPersistence(reader, store, TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isTrue();
        assertThat(store.listRecent(10)).hasSize(1);

        Alarm opened = store.open("svc", EventRule.WAY_THRESHOLD, "warning", "msg");
        Alarm resolved = opened.resolve(Instant.now());
        store.persistExisting(resolved);
        doThrow(new SQLException("fail")).when(ps).executeUpdate();
        sync.persist(opened);
    }

    @Test
    void skipsWhenSchemaNotReady() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.connection()).thenThrow(new SQLException("down"));
        AlarmPersistence sync = new AlarmPersistence(reader, new AlarmStore(TestMonitorRecordIds.create()), TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isFalse();
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

        AlarmPersistence sync = new AlarmPersistence(reader, new AlarmStore(TestMonitorRecordIds.create()), TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isFalse();
    }
}
