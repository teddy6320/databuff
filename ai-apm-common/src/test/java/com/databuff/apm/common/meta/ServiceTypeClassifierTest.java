package com.databuff.apm.common.meta;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTypeClassifierTest {

    @Test
    void classifiesDatabaseServices() {
        var classification = ServiceTypeClassifier.classify("[mysql]dc_databuff");
        assertThat(classification.serviceType()).isEqualTo("db");
        assertThat(classification.type()).isEqualTo("mysql");
    }

    @Test
    void classifiesElasticsearchAsDatabase() {
        var classification = ServiceTypeClassifier.classify("[elasticsearch]es:9200");
        assertThat(classification.serviceType()).isEqualTo("db");
        assertThat(classification.type()).isEqualTo("elasticsearch");
        assertThat(classification.technology()).isEqualTo("elasticsearch");
    }

    @Test
    void classifiesWebServices() {
        var classification = ServiceTypeClassifier.classify("demo-order");
        assertThat(classification.serviceType()).isEqualTo("web");
        assertThat(classification.type()).isEqualTo("web");
    }
}
