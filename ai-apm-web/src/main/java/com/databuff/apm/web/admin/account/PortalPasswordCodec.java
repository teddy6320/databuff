package com.databuff.apm.web.admin.account;

import com.databuff.apm.web.admin.support.AesCipherUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Portal password handling; legacy portal compatibility ({@code UserServiceImpl}):
 * store {@code AES(account + passwordDigest)} where the client sends MD5(password).
 */
public final class PortalPasswordCodec {

    private static final Pattern MD5_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}$");

    private PortalPasswordCodec() {
    }

    public static String md5Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    public static String encryptForAccount(String account, String clientPasswordDigest) {
        return AesCipherUtil.enCrypto(account + normalizeDigest(clientPasswordDigest), "AES");
    }

    public static boolean matches(String account, String storedCipher, String clientPassword) {
        if (account == null || storedCipher == null || clientPassword == null) {
            return false;
        }
        String digest = normalizeClientPassword(clientPassword);
        try {
            String decrypted = AesCipherUtil.deCrypto(storedCipher, "AES");
            return decrypted.equals(account + digest);
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    private static String normalizeClientPassword(String clientPassword) {
        String trimmed = clientPassword.trim();
        if (MD5_PATTERN.matcher(trimmed).matches()) {
            return trimmed.toLowerCase();
        }
        return md5Hex(trimmed);
    }

    private static String normalizeDigest(String digest) {
        return digest.trim().toLowerCase();
    }
}
