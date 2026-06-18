package com.databuff.apm.common.metric;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Portal-compatible metric definition (legacy {@code MetricsQuery}). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricQueryDefinition {

    private Long id;
    private String identifier;
    private String type1;
    private String type2;
    private String type3;
    private String app;
    private String database;
    private String measurement;
    private String field;
    private String desc;
    private Map<String, String> tagKey;
    private Map<String, String> tagValue;
    private Map<String, Object> fieldValue;
    private Map<String, Object> keys;
    private String unit;
    private String unitCn;
    private String metricCn;
    private String aggregatorType;
    private String formula;
    private Boolean isOpen;
    private Boolean core;
    private Boolean builtin;
    private String dorisTable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, String> getTagKey() {
        return tagKey;
    }

    public void setTagKey(Map<String, String> tagKey) {
        this.tagKey = tagKey;
    }

    public Map<String, String> getTagValue() {
        return tagValue;
    }

    public void setTagValue(Map<String, String> tagValue) {
        this.tagValue = tagValue;
    }

    public Map<String, Object> getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Map<String, Object> fieldValue) {
        this.fieldValue = fieldValue;
    }

    public Map<String, Object> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, Object> keys) {
        this.keys = keys;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitCn() {
        return unitCn;
    }

    public void setUnitCn(String unitCn) {
        this.unitCn = unitCn;
    }

    public String getMetricCn() {
        return metricCn;
    }

    public void setMetricCn(String metricCn) {
        this.metricCn = metricCn;
    }

    public String getAggregatorType() {
        return aggregatorType;
    }

    public void setAggregatorType(String aggregatorType) {
        this.aggregatorType = aggregatorType;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean open) {
        isOpen = open;
    }

    public Boolean getCore() {
        return core;
    }

    public void setCore(Boolean core) {
        this.core = core;
    }

    public Boolean getBuiltin() {
        return builtin;
    }

    public void setBuiltin(Boolean builtin) {
        this.builtin = builtin;
    }

    public String getDorisTable() {
        return dorisTable;
    }

    public void setDorisTable(String dorisTable) {
        this.dorisTable = dorisTable;
    }
}
