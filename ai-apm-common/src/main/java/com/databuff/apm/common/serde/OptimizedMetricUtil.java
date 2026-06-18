package com.databuff.apm.common.serde;

import com.databuff.apm.common.model.OptimizedMetric;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Compact binary codec for {@link OptimizedMetric}.
 * Layout: tsId(4) + timestamp(8) + measurement + tagValues[] + fieldValues[].
 */
public final class OptimizedMetricUtil {

    private OptimizedMetricUtil() {
    }

    public static byte[] serialize(OptimizedMetric metric) {
        byte[] measurementBytes = metric.measurement().getBytes(StandardCharsets.UTF_8);
        String[] tags = metric.tagValues();
        long[] fields = metric.fieldValues();

        int size = 4 + 8 + 2 + measurementBytes.length + 1;
        for (String tag : tags) {
            byte[] tagBytes = tag.getBytes(StandardCharsets.UTF_8);
            size += 2 + tagBytes.length;
        }
        size += 1 + fields.length * 8;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(metric.tsId());
        buffer.putLong(metric.timestamp());
        buffer.putShort((short) measurementBytes.length);
        buffer.put(measurementBytes);
        buffer.put((byte) tags.length);
        for (String tag : tags) {
            byte[] tagBytes = tag.getBytes(StandardCharsets.UTF_8);
            buffer.putShort((short) tagBytes.length);
            buffer.put(tagBytes);
        }
        buffer.put((byte) fields.length);
        for (long field : fields) {
            buffer.putLong(field);
        }
        return buffer.array();
    }

    public static OptimizedMetric deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int tsId = buffer.getInt();
        long timestamp = buffer.getLong();
        int measurementLen = Short.toUnsignedInt(buffer.getShort());
        byte[] measurementBytes = new byte[measurementLen];
        buffer.get(measurementBytes);
        String measurement = new String(measurementBytes, StandardCharsets.UTF_8);

        int tagCount = Byte.toUnsignedInt(buffer.get());
        String[] tags = new String[tagCount];
        for (int i = 0; i < tagCount; i++) {
            int tagLen = Short.toUnsignedInt(buffer.getShort());
            byte[] tagBytes = new byte[tagLen];
            buffer.get(tagBytes);
            tags[i] = new String(tagBytes, StandardCharsets.UTF_8);
        }

        int fieldCount = Byte.toUnsignedInt(buffer.get());
        long[] fields = new long[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            fields[i] = buffer.getLong();
        }

        return new OptimizedMetric()
                .withTsId(tsId)
                .withTimestamp(timestamp)
                .withMeasurement(measurement)
                .withTagValues(tags)
                .withFieldValues(fields);
    }

    public static int readTsId(byte[] bytes) {
        if (bytes.length < 4) {
            return 0;
        }
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static long readTimestamp(byte[] bytes) {
        if (bytes.length < 12) {
            return 0L;
        }
        return ByteBuffer.wrap(bytes).getLong(4);
    }

    public static OptimizedMetric mergeSerialized(byte[] left, byte[] right) {
        return deserialize(left).merge(deserialize(right));
    }

    public static byte[] mergeSerializedBytes(byte[] left, byte[] right) {
        if (left.length == 0) {
            return Arrays.copyOf(right, right.length);
        }
        if (right.length == 0) {
            return Arrays.copyOf(left, left.length);
        }
        if (!isOptimizedFormat(left) || !isOptimizedFormat(right)) {
            byte[] merged = new byte[left.length + right.length];
            System.arraycopy(left, 0, merged, 0, left.length);
            System.arraycopy(right, 0, merged, left.length, right.length);
            return merged;
        }
        return serialize(mergeSerialized(left, right));
    }

    public static boolean isOptimizedFormat(byte[] bytes) {
        if (bytes == null || bytes.length < 15 || bytes[0] == '{') {
            return false;
        }
        int measurementLen = Short.toUnsignedInt(ByteBuffer.wrap(bytes, 12, 2).getShort());
        return measurementLen >= 0 && 15 + measurementLen <= bytes.length;
    }
}
