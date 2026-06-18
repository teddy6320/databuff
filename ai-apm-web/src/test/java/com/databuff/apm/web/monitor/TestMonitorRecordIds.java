package com.databuff.apm.web.monitor;

public final class TestMonitorRecordIds {

    private TestMonitorRecordIds() {
    }

    public static MonitorRecordIdGenerator create() {
        return new MonitorRecordIdGenerator(1L);
    }
}
