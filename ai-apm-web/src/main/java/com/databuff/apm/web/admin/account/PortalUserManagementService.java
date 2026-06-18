package com.databuff.apm.web.admin.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PortalUserManagementService {

    private final PortalUserAccount builtInAdmin;

    public PortalUserManagementService(
            @Value("${apm.security.seed-username:admin}") String seedUsername,
            @Value("${apm.security.seed-password:Databuff@123}") String seedPassword) {
        Instant now = Instant.now();
        String account = seedUsername.trim();
        builtInAdmin = new PortalUserAccount(
                1L,
                account,
                PortalPasswordCodec.encryptForAccount(account, PortalPasswordCodec.md5Hex(seedPassword)),
                account,
                "",
                "",
                "",
                true,
                null,
                now,
                now);
    }

    public boolean authenticate(String account, String password) {
        if (!matchesAccount(account)) {
            return false;
        }
        return PortalPasswordCodec.matches(builtInAdmin.account(), builtInAdmin.passwordHash(), password);
    }

    public Optional<PortalUserAccount> findByAccount(String account) {
        return matchesAccount(account) ? Optional.of(builtInAdmin) : Optional.empty();
    }

    private boolean matchesAccount(String account) {
        if (account == null || account.isBlank()) {
            return false;
        }
        return builtInAdmin.account().equalsIgnoreCase(account.trim());
    }
}
