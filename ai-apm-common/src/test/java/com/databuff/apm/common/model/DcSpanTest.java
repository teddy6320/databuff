package com.databuff.apm.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DcSpanTest {

    @Test
    void serializesDottedMetaFields() throws Exception {
        DcSpan span = new DcSpan();
        span.service = "checkout";
        span.name = "GET";
        span.metaHttpStatusCode = 200;
        span.metaHttpMethod = "GET";
        span.metaHttpUrl = "/api";

        String json = new ObjectMapper().writeValueAsString(span);
        assertThat(json).contains("meta.http.status_code");
        assertThat(json).contains("checkout");
    }
}
