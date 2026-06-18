package com.databuff.apm.web.flow;

import com.databuff.apm.common.query.ApmQueryModels.ServiceFlowTreeRow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleServiceFlowTreeBuilderTest {

    @Test
    void buildsRootAndChildNodes() {
        ServiceFlowTreeRow root = new ServiceFlowTreeRow(
                "root-path", "", "gateway", "gateway-id", "GET /", 1, 10, 0, 10, 1000);
        ServiceFlowTreeRow child = new ServiceFlowTreeRow(
                "child-path", "root-path", "checkout", "checkout-id", "POST /pay", 1, 5, 1, 5, 500);

        Map<String, Map<String, Object>> flows = MultipleServiceFlowTreeBuilder.build(
                List.of(root, child), null, null);

        assertThat(flows).containsKey("gateway");
        Map<String, Object> rootNode = flows.get("gateway");
        assertThat(rootNode.get("call")).isEqualTo(10L);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) rootNode.get("children");
        assertThat(children).hasSize(1);
        assertThat(children.get(0).get("service")).isEqualTo("checkout");
    }

    @Test
    void filtersSqlResourcesFromEntryNode() {
        ServiceFlowTreeRow root = new ServiceFlowTreeRow(
                "root-path", "", "service-a", "service-a-id", "GET /demo/checkout", 1, 10, 0, 10, 1000);
        ServiceFlowTreeRow dbRow = new ServiceFlowTreeRow(
                "db-path", "root-path", "service-a", "service-a-id", "INSERT INTO demo_order VALUES (?)", 1, 4, 0, 4, 400);

        Map<String, Map<String, Object>> flows = MultipleServiceFlowTreeBuilder.build(
                List.of(root, dbRow), null, null);

        @SuppressWarnings("unchecked")
        List<String> resources = (List<String>) flows.get("service-a").get("resources");
        assertThat(resources).containsExactly("GET /demo/checkout");
    }
}
