package com.databuff.apm.web.portal;

import com.databuff.apm.web.monitor.policy.ResponsePolicyService;

import com.databuff.apm.web.metric.MetricQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EventPortalServiceTest {

    private EventPortalService service;

    @BeforeEach
    void setUp() {
        service = new EventPortalService(
                null,
                null,
                null,
                new ResponsePolicyService(),
                mock(MetricQueryService.class),
                Mockito.mock(MetricPortalService.class));
    }

    @Test
    void responsePolicyListStartsEmpty() {
        Map<String, Object> response = service.searchResponsePolicies(Map.of("pageNum", 1, "pageSize", 20));

        assertThat(response.get("status")).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertThat(list).isEmpty();
    }
}
