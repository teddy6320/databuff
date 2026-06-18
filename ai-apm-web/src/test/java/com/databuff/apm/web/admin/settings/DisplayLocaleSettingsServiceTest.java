package com.databuff.apm.web.admin.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisplayLocaleSettingsServiceTest {

    @Test
    void defaultsToChinese() {
        DisplayLocaleSettingsService service = new DisplayLocaleSettingsService();
        assertThat(service.locale()).isEqualTo(DisplayLocaleSettingsService.DEFAULT_LOCALE);
    }

    @Test
    void updatesSupportedLocales() {
        DisplayLocaleSettingsService service = new DisplayLocaleSettingsService();
        service.updateLocale("en-US");
        assertThat(service.locale()).isEqualTo("en-US");
    }

    @Test
    void rejectsUnsupportedLocale() {
        DisplayLocaleSettingsService service = new DisplayLocaleSettingsService();
        assertThatThrownBy(() -> service.updateLocale("ja-JP"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
