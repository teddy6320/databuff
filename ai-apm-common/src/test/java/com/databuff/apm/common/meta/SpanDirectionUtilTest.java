package com.databuff.apm.common.meta;

import com.databuff.apm.common.model.DcSpan;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpanDirectionUtilTest {

    @Test
    void httpServerNameMarksInbound() {
        DcSpan span = httpSpan("GET /demo/checkout", "SPAN_KIND_SERVER");
        assertThat(SpanDirectionUtil.httpDirection(span)).isEqualTo(new SpanDirectionUtil.Direction(1, 0));
    }

    @Test
    void httpClientNameMarksOutbound() {
        DcSpan span = httpSpan("HTTP GET service-b /api/orders", "SPAN_KIND_CLIENT");
        assertThat(SpanDirectionUtil.httpDirection(span)).isEqualTo(new SpanDirectionUtil.Direction(0, 1));
    }

    @Test
    void dbClientMarksInboundAndOutbound() {
        DcSpan span = new DcSpan();
        span.type = "SPAN_KIND_CLIENT";
        span.name = "INSERT demo_order_audit";
        span.meta = "{\"db.system\":\"mysql\",\"db.statement\":\"INSERT INTO t VALUES (?)\"}";
        assertThat(SpanDirectionUtil.resolve(span)).isEqualTo(new SpanDirectionUtil.Direction(1, 1));
    }

    @Test
    void applyNameBasedDirectionUpdatesSpan() {
        DcSpan span = httpSpan("GET /demo/checkout", "SPAN_KIND_SERVER");
        SpanDirectionUtil.applyNameBasedDirection(span);
        assertThat(span.isIn).isEqualTo(1);
        assertThat(span.isOut).isZero();
    }

    private static DcSpan httpSpan(String name, String kind) {
        DcSpan span = new DcSpan();
        span.name = name;
        span.resource = name;
        span.type = kind;
        span.metaHttpMethod = "GET";
        span.metaHttpStatusCode = 200;
        return span;
    }
}
