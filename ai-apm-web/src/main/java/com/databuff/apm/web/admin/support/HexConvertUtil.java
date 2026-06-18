package com.databuff.apm.web.admin.support;

/** Hex helpers aligned with databuff-portal {@code HexConvertUtil}. */
public final class HexConvertUtil {

    private HexConvertUtil() {
    }

    public static String parseByte2HexStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte buff : bytes) {
            String hex = Integer.toHexString(buff & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) {
            return new byte[0];
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0, len = hexStr.length() / 2; i < len; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
}
