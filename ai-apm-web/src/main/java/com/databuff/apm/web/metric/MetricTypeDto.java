package com.databuff.apm.web.metric;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.TreeSet;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricTypeDto implements Comparable<MetricTypeDto> {

    private String type1;
    private String type2;
    private String type3;
    private String app;
    private Boolean builtin;
    private TreeSet<String> metricList = new TreeSet<>();

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
        this.type2 = type2;
    }

    public String getType3() {
        return type3;
    }

    public void setType3(String type3) {
        this.type3 = type3;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Boolean getBuiltin() {
        return builtin;
    }

    public void setBuiltin(Boolean builtin) {
        this.builtin = builtin;
    }

    public TreeSet<String> getMetricList() {
        return metricList;
    }

    public void setMetricList(TreeSet<String> metricList) {
        this.metricList = metricList;
    }

    @Override
    public int compareTo(MetricTypeDto other) {
        int result = compareNullable(type1, other.type1);
        if (result != 0) {
            return result;
        }
        result = compareNullable(type2, other.type2);
        if (result != 0) {
            return result;
        }
        result = compareNullable(type3, other.type3);
        if (result != 0) {
            return result;
        }
        return compareNullable(app, other.app);
    }

    private static int compareNullable(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MetricTypeDto dto)) {
            return false;
        }
        return java.util.Objects.equals(type1, dto.type1)
                && java.util.Objects.equals(type2, dto.type2)
                && java.util.Objects.equals(type3, dto.type3)
                && java.util.Objects.equals(app, dto.app);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(type1, type2, type3, app);
    }
}
