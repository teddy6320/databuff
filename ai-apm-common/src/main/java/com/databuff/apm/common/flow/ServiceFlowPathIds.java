package com.databuff.apm.common.flow;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Path id helpers; legacy portal compatibility ({@code FillPathAndRelationUtil#getMD5Long}). */
public final class ServiceFlowPathIds {

    private ServiceFlowPathIds() {
    }

    public static String entryPathId(String serviceId) {
        return pathId(serviceId == null ? "" : serviceId);
    }

    public static String entryInterfacePathId(String serviceId, String resource) {
        String service = serviceId == null ? "" : serviceId;
        String res = resource == null ? "" : resource;
        return pathId(service + res);
    }

    public static String pathId(String parentPathId, int level, String serviceId) {
        String parent = parentPathId == null ? "" : parentPathId;
        String service = serviceId == null ? "" : serviceId;
        return pathId(parent + "_" + level + "_" + service);
    }

    public static String interfacePathId(String parentInterfacePathId, int level, String serviceId, String resource) {
        String parent = parentInterfacePathId == null ? "" : parentInterfacePathId;
        String service = serviceId == null ? "" : serviceId;
        String res = resource == null ? "" : resource;
        return pathId(parent + "_" + level + "_" + service + res);
    }

    public static String pathId(String input) {
        return String.valueOf(md5Long(input));
    }

    static long md5Long(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            long value = 0L;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (digest[i] & 0xFF);
            }
            return value;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
