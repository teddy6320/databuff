package com.databuff.apm.web.monitor;

public final class ThresholdAlarmCheck {

    private ThresholdAlarmCheck() {
    }

    public static boolean breached(double actual, double threshold, String comparator) {
        if (EventRule.COMPARATOR_GT.equals(comparator)) {
            return actual > threshold;
        }
        return actual >= threshold;
    }
}
