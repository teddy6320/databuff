package com.databuff.apm.web.cockpit;

import com.databuff.apm.common.query.ApmQueryModels.TrafficLightPoint;

import com.databuff.apm.common.storage.ApmReadRepository;
import com.databuff.apm.web.persistence.TrafficLightConfigPersistence;
import com.databuff.apm.web.admin.settings.SessionIdleSettingsService;
import com.databuff.apm.web.auth.JwtTokenService;
import com.databuff.apm.web.config.ApmStorageProperties;
import com.databuff.apm.web.config.AuthConfiguration;
import com.databuff.apm.web.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CockpitController.class)
@Import({TrafficLightService.class, AuthConfiguration.class})
@EnableConfigurationProperties({ApmStorageProperties.class, JwtProperties.class})
@TestPropertySource(properties = {
        "apm.security.jwt.secret=dev-secret",
        "apm.security.jwt.expiration-seconds=3600"
})
class CockpitControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private ApmReadRepository readRepository;

    @MockitoBean
    private TrafficLightConfigPersistence configSync;

    @MockitoBean
    private SessionIdleSettingsService sessionIdleSettingsService;

    @BeforeEach
    void setUp() {
        when(sessionIdleSettingsService.idleSeconds()).thenReturn(SessionIdleSettingsService.DEFAULT_IDLE_SECONDS);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtTokenService.issueToken("admin"));
        return headers;
    }

    @Test
    void trafficLightEndpoint() throws Exception {
        when(readRepository.queryTrafficLight(anyString()))
                .thenReturn(List.of(new TrafficLightPoint(
                        "2026-06-01 12:00:00", "checkout", 2, 20)));

        mockMvc.perform(post("/webapi/api/v1/cockpit/trafficLight?from=0&to=1000")
                        .headers(authHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("checkout"));
    }

    @Test
    void configEndpoints() throws Exception {
        mockMvc.perform(get("/webapi/api/v1/cockpit/config")
                        .headers(authHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorRateThreshold").exists());
    }
}
