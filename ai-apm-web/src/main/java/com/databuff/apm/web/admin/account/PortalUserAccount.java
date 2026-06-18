package com.databuff.apm.web.admin.account;

import java.time.Instant;

public record PortalUserAccount(
        long id,
        String account,
        String passwordHash,
        String responsible,
        String mobile,
        String email,
        String remark,
        boolean builtIn,
        Instant lockedUntil,
        Instant createdAt,
        Instant updatedAt) {
}
