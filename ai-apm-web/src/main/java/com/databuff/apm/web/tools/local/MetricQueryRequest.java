package com.databuff.apm.web.tools.local;

import java.util.List;

public class MetricQueryRequest {

    private String measurement;
    private List<MetricQueryAggregation> aggregations;
    private List<MetricQueryWhere> wheres;
    private List<String> groupBy;
    private Integer interval;
    private String intervalUnit;
    private String start;
    private String end;

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public List<MetricQueryAggregation> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<MetricQueryAggregation> aggregations) {
        this.aggregations = aggregations;
    }

    public List<MetricQueryWhere> getWheres() {
        return wheres;
    }

    public void setWheres(List<MetricQueryWhere> wheres) {
        this.wheres = wheres;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(String intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
