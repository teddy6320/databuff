package com.databuff.apm.web.admin.settings;

import org.springframework.stereotype.Service;

@Service
public class DisplayLocaleSettingsService {

    public static final String DEFAULT_LOCALE = "zh-CN";

    private volatile String locale = DEFAULT_LOCALE;

    public String locale() {
        return locale;
    }

    public void updateLocale(String next) {
        if (!"zh-CN".equals(next) && !"en-US".equals(next)) {
            throw new IllegalArgumentException("unsupported display locale: " + next);
        }
        locale = next;
    }
}
