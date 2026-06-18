package com.databuff.apm.web.portal;

import com.alibaba.druid.pool.DruidDataSource;
import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.common.storage.DorisConnectionConfig;
import com.databuff.apm.web.TestStorageSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Live Doris smoke test — run with {@code DORIS_LIVE=1 DORIS_FE_HOST=192.168.50.140 mvn -pl ai-apm-web test -Dtest=ServicePortalRpcLiveTest}. */
@EnabledIfEnvironmentVariable(named = "DORIS_LIVE", matches = "1")
class ServicePortalRpcLiveTest {

  private static ServicePortalService service;

  @BeforeAll
  static void connect() throws Exception {
    DorisConnectionConfig config = DorisConnectionConfig.fromEnv();
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource.setUrl(config.feJdbcUrl());
    dataSource.setUsername("root");
    dataSource.setPassword("");
    dataSource.init();
    service = TestStorageSupport.servicePortalService(new ApmReadRepository(dataSource));
  }

  @Test
  void rpcCallInfoEndpointsAndGraphStatsReturnData() {
    Map<String, Object> body = Map.of(
            "componentType", "service.rpc",
            "serviceId", "5457a0119281bb98",
            "srcServiceId", "9bf61532d56eb7b5",
            "fromTime", "2026-06-05 21:00:00",
            "toTime", "2026-06-06 01:00:00");

    Map<String, Object> info = service.callInfo(body);
    assertThat(info.get("reqInCnt")).isEqualTo(12L);
    assertThat(info.get("reqOutCnt")).isEqualTo(12L);

    Map<String, Object> endpoints = service.callEndpoints(body);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> rows = (List<Map<String, Object>>) endpoints.get("data");
    assertThat(rows).isNotEmpty();
    assertThat(rows.get(0).get("reqInCnt")).isEqualTo(12L);

    Map<String, Object> inGraph = service.callGraphStats(Map.of(
            "componentType", "service.rpc",
            "serviceId", "5457a0119281bb98",
            "srcServiceId", "9bf61532d56eb7b5",
            "isIn", 1,
            "fromTime", "2026-06-05 21:00:00",
            "toTime", "2026-06-06 01:00:00"));
    @SuppressWarnings("unchecked")
    Map<String, Number> inCnts = (Map<String, Number>) inGraph.get("callCnts");
    assertThat(inCnts.values().stream().filter(v -> v != null).mapToLong(Number::longValue).sum()).isEqualTo(12L);

    Map<String, Object> outGraph = service.callGraphStats(Map.of(
            "componentType", "service.rpc",
            "serviceId", "5457a0119281bb98",
            "srcServiceId", "9bf61532d56eb7b5",
            "isOut", 1,
            "fromTime", "2026-06-05 21:00:00",
            "toTime", "2026-06-06 01:00:00"));
    @SuppressWarnings("unchecked")
    Map<String, Number> outCnts = (Map<String, Number>) outGraph.get("callCnts");
    assertThat(outCnts.values().stream().filter(v -> v != null).mapToLong(Number::longValue).sum()).isEqualTo(12L);
  }
}
