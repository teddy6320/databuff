package com.databuff.apm.web.admin.support;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * AES password codec aligned with databuff-portal {@code AesCipherUtil}.
 * Passwords are stored as {@code AES(account + md5Password)}.
 */
public final class AesCipherUtil {

    private static final String ENCRYPT_AES_KEY = "V2FuZzkyNjQ1NGRTQkFQSUpXVA==";

    private AesCipherUtil() {
    }

    public static String enCrypto(String str, String transformation) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(Base64ConvertUtil.decode(ENCRYPT_AES_KEY).getBytes(StandardCharsets.UTF_8));
            keygen.init(128, secureRandom);
            SecretKey deskey = keygen.generateKey();
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, deskey);
            byte[] cipherByte = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return Base64ConvertUtil.encode(HexConvertUtil.parseByte2HexStr(cipherByte));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException("AES encrypt failed: " + e.getMessage(), e);
        }
    }

    public static String deCrypto(String str, String transformation) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(Base64ConvertUtil.decode(ENCRYPT_AES_KEY).getBytes(StandardCharsets.UTF_8));
            keygen.init(128, secureRandom);
            SecretKey deskey = keygen.generateKey();
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            byte[] cipherByte = cipher.doFinal(HexConvertUtil.parseHexStr2Byte(Base64ConvertUtil.decode(str)));
            return new String(cipherByte, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalStateException("AES decrypt failed: " + e.getMessage(), e);
        }
    }
}
