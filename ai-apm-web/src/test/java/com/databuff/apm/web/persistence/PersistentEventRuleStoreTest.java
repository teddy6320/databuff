package com.databuff.apm.web.persistence;

import com.databuff.apm.web.TestStorageSupport;
import com.databuff.apm.web.monitor.EventRule;
import com.databuff.apm.web.monitor.EventRuleStore;
import com.databuff.apm.web.monitor.InMemoryEventRuleStore;
import com.databuff.apm.common.storage.ApmReadRepository;
import org.assertj.core.api.Assertions;
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

class PersistentEventRuleStoreTest {

    @Test
    void delegatesToMemoryWhenDorisUnavailable() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        when(reader.connection()).thenThrow(new SQLException("down"));
        InMemoryEventRuleStore memory = new InMemoryEventRuleStore();
        PersistentEventRuleStore store = new PersistentEventRuleStore(memory, reader, TestStorageSupport.storage());
        store.reloadFromStore();
        Assertions.assertThat(store.list()).isEmpty();
        EventRule created = store.create(new EventRuleStore.CreateRequest(
                "svc spike", "checkout", 0.5, "gt", true, EventRule.WAY_MUTATION));
        assertThat(created.detectionWay()).isEqualTo(EventRule.WAY_MUTATION);
    }

    @Test
    void loadsRowsAndPersistsLifecycle() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet schemaRs = mock(ResultSet.class);
        ResultSet rulesRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(reader.connection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(contains("config_event_rule"))).thenReturn(ps);
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("LIMIT 1")) {
                return schemaRs;
            }
            return rulesRs;
        });
        when(schemaRs.next()).thenReturn(true);
        when(rulesRs.next()).thenReturn(true, false);
        when(rulesRs.getLong("id")).thenReturn(9L);
        when(rulesRs.getString("rule_name")).thenReturn("from-doris");
        when(rulesRs.getString("classify")).thenReturn(EventRule.CLASSIFY_SINGLE);
        when(rulesRs.getString("detection_way")).thenReturn(EventRule.WAY_THRESHOLD);
        when(rulesRs.getString("service")).thenReturn("checkout");
        when(rulesRs.getString("metric")).thenReturn(EventRule.METRIC_ERROR_RATE);
        when(rulesRs.getDouble("threshold")).thenReturn(0.1);
        when(rulesRs.getString("comparator")).thenReturn(EventRule.COMPARATOR_GT);
        when(rulesRs.getInt("enabled")).thenReturn(1);

        InMemoryEventRuleStore memory = new InMemoryEventRuleStore();
        PersistentEventRuleStore store = new PersistentEventRuleStore(memory, reader, TestStorageSupport.storage());
        store.reloadFromStore();

        Assertions.assertThat(store.list()).extracting(EventRule::ruleName).contains("from-doris");
        EventRule created = store.create(new EventRuleStore.CreateRequest(
                "persist me", "checkout", 0.2, "gt", true));
        EventRule updated = store.save(created.withEnabled(false));
        assertThat(updated.enabled()).isFalse();
        Assertions.assertThat(store.findById(created.id())).isPresent();
        assertThat(store.delete(created.id())).isTrue();
    }

    @Test
    void toleratesPersistAndDeleteFailures() throws Exception {
        ApmReadRepository reader = mock(ApmReadRepository.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet schemaRs = mock(ResultSet.class);
        ResultSet rulesRs = mock(ResultSet.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(reader.connection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(contains("config_event_rule"))).thenReturn(ps);
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("LIMIT 1")) {
                return schemaRs;
            }
            return rulesRs;
        });
        when(schemaRs.next()).thenReturn(true);
        when(rulesRs.next()).thenReturn(false);

        InMemoryEventRuleStore memory = new InMemoryEventRuleStore();
        PersistentEventRuleStore store = new PersistentEventRuleStore(memory, reader, TestStorageSupport.storage());
        store.reloadFromStore();

        doThrow(new SQLException("persist failed")).when(ps).executeUpdate();
        EventRule created = store.create(new EventRuleStore.CreateRequest(
                "fail persist", "checkout", 0.2, "gt", true));
        store.delete(created.id());
    }

    @Test
    void toleratesLoadFailureAfterSchemaReady() throws Exception {
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

        InMemoryEventRuleStore memory = new InMemoryEventRuleStore();
        PersistentEventRuleStore store = new PersistentEventRuleStore(memory, reader, TestStorageSupport.storage());
        store.reloadFromStore();
        Assertions.assertThat(store.list()).isEmpty();
    }
}