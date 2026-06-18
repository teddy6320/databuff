package com.databuff.apm.web.admin.controller;

import com.databuff.apm.web.admin.settings.DisplayLocaleSettingsService;
import com.databuff.apm.web.admin.settings.SessionIdleSettingsService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SystemPortalControllerTest {

    private SystemPortalController controller(
            SessionIdleSettingsService sessionIdleSettingsService,
            DisplayLocaleSettingsService displayLocaleSettingsService) {
        return new SystemPortalController(sessionIdleSettingsService, displayLocaleSettingsService);
    }

    @Test
    void systemBaseReturnsConfiguredIdleTimeoutAndDefaultLocale() {
        SessionIdleSettingsService settings = new SessionIdleSettingsService();
        DisplayLocaleSettingsService localeSettings = new DisplayLocaleSettingsService();
        SystemPortalController controller = controller(settings, localeSettings);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) controller.systemBase().get("data");

        assertThat(data.get("pageTimeOut")).isEqualTo(SessionIdleSettingsService.DEFAULT_IDLE_SECONDS);
        assertThat(data.get("locale")).isEqualTo(DisplayLocaleSettingsService.DEFAULT_LOCALE);
    }

    @Test
    void logoConfigReturnsDefaultLocale() {
        SystemPortalController controller = controller(
                new SessionIdleSettingsService(),
                new DisplayLocaleSettingsService());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) controller.logoConfig().get("data");

        assertThat(data.get("locale")).isEqualTo(DisplayLocaleSettingsService.DEFAULT_LOCALE);
    }

    @Test
    void updatePageTimeOutPersistsValue() {
        SessionIdleSettingsService settings = new SessionIdleSettingsService();
        SystemPortalController controller = controller(settings, new DisplayLocaleSettingsService());

        Map<String, Object> response = controller.updatePageTimeOut(Map.of("pageTimeOut", 7200));

        assertThat(response.get("status")).isEqualTo(200);
        assertThat(settings.idleSeconds()).isEqualTo(7200);
    }

    @Test
    void updateDisplayLocalePersistsValue() {
        DisplayLocaleSettingsService localeSettings = new DisplayLocaleSettingsService();
        SystemPortalController controller = controller(new SessionIdleSettingsService(), localeSettings);

        Map<String, Object> response = controller.updateDisplayLocale(Map.of("locale", "en-US"));

        assertThat(response.get("status")).isEqualTo(200);
        assertThat(localeSettings.locale()).isEqualTo("en-US");
    }

    @Test
    void updateDisplayLocaleRejectsInvalidValue() {
        SystemPortalController controller = controller(
                new SessionIdleSettingsService(),
                new DisplayLocaleSettingsService());

        Map<String, Object> response = controller.updateDisplayLocale(Map.of("locale", "ja-JP"));

        assertThat(response.get("status")).isEqualTo(400);
    }

    @Test
    void updatePageTimeOutRejectsInvalidValue() {
        SystemPortalController controller = controller(
                new SessionIdleSettingsService(),
                new DisplayLocaleSettingsService());

        Map<String, Object> response = controller.updatePageTimeOut(Map.of("pageTimeOut", 0));

        assertThat(response.get("status")).isEqualTo(400);
    }
}
