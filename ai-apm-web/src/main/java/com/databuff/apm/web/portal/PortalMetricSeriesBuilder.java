package com.databuff.apm.web.portal;

import com.databuff.apm.common.query.ApmQueryModels.MetricSeriesPoint;
import com.databuff.apm.common.query.TimeSeriesFillUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds portal TSDBSeries-shaped maps for metric_stats / resource_stats. */
final class PortalMetricSeriesBuilder {

    private PortalMetricSeriesBuilder() {
    }

    static Map<String, Object> series(
            Map<String, String> tags,
            List<MetricSeriesPoint> points,
            String valueColumn,
            long fromMillis,
            long toMillis,
            int intervalSec) {
        List<MetricSeriesPoint> filled =
                TimeSeriesFillUtil.fillMetricSeries(points, fromMillis, toMillis, intervalSec);
        return series(tags, filled, valueColumn);
    }

    static Map<String, Object> series(
            Map<String, String> tags, List<MetricSeriesPoint> points, String valueColumn) {
        List<List<Number>> values = new ArrayList<>();
        for (MetricSeriesPoint point : points) {
            values.add(Arrays.asList(point.epochSeconds() * 1000L, point.value()));
        }
        Map<String, Object> series = new LinkedHashMap<>();
        series.put("tags", tags);
        series.put("columns", List.of("time", valueColumn));
        series.put("values", values);
        series.put("rootDetails", List.of());
        return series;
    }
}
