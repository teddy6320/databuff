package com.databuff.apm.web.persistence;

import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.ai.TestBeanSupport;
import com.databuff.apm.web.monitor.NotifyChannelService;

import com.databuff.apm.common.storage.ApmReadRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyChannelPersistenceTest {

    @Test
    void hydratesAndPersists() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet schemaRs = mock(ResultSet.class);
        ResultSet rowRs = mock(ResultSet.class);
        when(reader.connection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(contains("config_notify_channel"))).thenReturn(ps);
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("SELECT 1") && sql.contains("config_notify_channel")) {
                return schemaRs;
            }
            return rowRs;
        });
        when(schemaRs.next()).thenReturn(true);
        when(rowRs.next()).thenReturn(true);
        when(rowRs.getLong("id")).thenReturn(1L);
        when(rowRs.getString("channel_type")).thenReturn("webhook");
        when(rowRs.getString("webhook_url")).thenReturn("https://hook");
        when(rowRs.getInt("enabled")).thenReturn(1);

        NotifyChannelService service = TestBeanSupport.notifyChannelService();
        NotifyChannelPersistence sync = new NotifyChannelPersistence(reader, service, TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isTrue();
        assertThat(service.getConfig().get("webhookUrl")).isEqualTo("https://hook");

        sync.persist("https://new", true);
        doThrow(new SQLException("fail")).when(ps).executeUpdate();
        sync.persist("https://fail", false);
    }

    @Test
    void skipsWhenSchemaNotReady() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.connection()).thenThrow(new SQLException("down"));
        NotifyChannelPersistence sync = new NotifyChannelPersistence(reader, TestBeanSupport.notifyChannelService(), TestStorageSupport.storage());
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
            if (sql.contains("SELECT 1")) {
                return schemaRs;
            }
            throw new SQLException("load failed");
        });
        when(schemaRs.next()).thenReturn(true);

        NotifyChannelPersistence sync = new NotifyChannelPersistence(reader, TestBeanSupport.notifyChannelService(), TestStorageSupport.storage());
        sync.reloadFromStore();
        assertThat(sync.persistenceEnabled()).isFalse();
    }
}
