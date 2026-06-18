package com.databuff.apm.web.ai.platform.runtime;

import com.databuff.apm.web.ai.TestBeanSupport;
import com.databuff.apm.web.monitor.service.AlarmService;
import com.databuff.apm.web.ai.platform.tool.AiToolDefinition;
import com.databuff.apm.web.ai.platform.tool.ToolType;
import com.databuff.apm.web.portal.ServicePortalService;
import com.databuff.apm.web.portal.TracePortalService;
import com.databuff.apm.web.tools.local.CommonTools;
import com.databuff.apm.web.tools.local.DataTools;
import com.databuff.apm.web.tools.local.TimeTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.Toolkit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AgentScopeToolFactoryTest {

    private AgentScopeToolFactory factory;

    @BeforeEach
    void setUp() {
        DataTools dataTools = TestBeanSupport.dataTools(
                mock(ServicePortalService.class),
                mock(TracePortalService.class),
                Mockito.mock(AlarmService.class),
                new ObjectMapper());
        ObjectMapper objectMapper = new ObjectMapper();
        factory = TestBeanSupport.agentScopeToolFactory(
                dataTools,
                new TimeTool(),
                new CommonTools(objectMapper),
                TestBeanSupport.inspectTools(mock(ServicePortalService.class), objectMapper),
                null);
    }

    @Test
    void registersTimeToolsOnce() {
        Toolkit toolkit = new Toolkit();
        factory.registerTools(toolkit, List.of(
                tool("time.getCurrentTimeRange", "timeTool.getCurrentTimeRange"),
                tool("time.getTimeRangeAroundTime", "timeTool.getTimeRangeAroundTime")));
        assertThat(toolkit.getToolNames())
                .contains("getCurrentTimeRange", "getTimeRangeAroundTime");
    }

    @Test
    void registersCommonToolsOnce() {
        Toolkit toolkit = new Toolkit();
        factory.registerTools(toolkit, List.of(
                tool("common.getCurrentTimeRange", "commonTools.getCurrentTimeRange"),
                tool("common.drawTrendCharts", "commonTools.drawTrendCharts")));
        assertThat(toolkit.getToolNames())
                .contains("getCurrentTimeRange", "getTimeRangeAroundTime", "drawTrendCharts");
    }

    @Test
    void registersInspectToolsOnce() {
        Toolkit toolkit = new Toolkit();
        factory.registerTools(toolkit, List.of(
                tool("inspect.inspectService", "inspectTools.inspectService")));
        assertThat(toolkit.getToolNames()).contains("inspectService");
    }

    @Test
    void registersDataToolsOnce() {
        Toolkit toolkit = new Toolkit();
        factory.registerTools(toolkit, List.of(
                tool("data.queryServicesAll", "dataTools.queryServicesAll"),
                tool("data.queryTraceDetail", "dataTools.queryTraceDetail")));
        assertThat(toolkit.getToolNames())
                .contains(
                        "queryServicesAll",
                        "queryServicesByServiceType",
                        "queryServiceTopology",
                        "queryTraceListByCondition",
                        "queryTraceDetail",
                        "queryServiceAlarms",
                        "queryMetricData");
    }

    @Test
    void skipsUnknownImplementation() {
        Toolkit toolkit = new Toolkit();
        factory.registerTools(toolkit, List.of(
                tool("custom.tool", "unknownBean.method")));
        assertThat(toolkit.getToolNames()).isEmpty();
    }

    private static AiToolDefinition tool(String toolId, String implementation) {
        Instant now = Instant.now();
        return new AiToolDefinition(
                toolId, toolId, null, "desc", ToolType.JAVA_BEAN, implementation,
                "{}", "{}", true, true, 1L, now, now);
    }
}
