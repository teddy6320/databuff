package com.databuff.apm.common.serde;

import com.databuff.apm.common.meta.OtelAttributeMaps;
import com.databuff.apm.common.model.DcSpan;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

public final class DCSpanJsonEncoder {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private DCSpanJsonEncoder() {
    }

    public static byte[] encode(DcSpan span) throws IOException {
        OtelAttributeMaps.materialize(span);
        return MAPPER.writeValueAsBytes(span);
    }
}
