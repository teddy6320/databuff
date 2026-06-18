package com.databuff.apm.web.admin.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PortalUserManagementServiceTest {

    private final PortalUserManagementService service = new PortalUserManagementService("admin", "Databuff@123");

    @Test
    void seedAdminCanLoginWithMd5OrPlainPassword() {
        assertThat(service.authenticate("admin", PortalPasswordCodec.md5Hex("Databuff@123"))).isTrue();
        assertThat(service.authenticate("admin", "Databuff@123")).isTrue();
        assertThat(service.authenticate("admin", "wrong")).isFalse();
    }

    @Test
    void onlyBuiltInAdminExists() {
        assertThat(service.findByAccount("admin")).isPresent();
        assertThat(service.findByAccount("other")).isEmpty();
    }
}
