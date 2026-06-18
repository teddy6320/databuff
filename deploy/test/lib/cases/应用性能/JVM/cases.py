"""JVM 指标（服务详情 tab-jvm 对齐）"""

from __future__ import annotations

from pathlib import Path

from ...common import (
    DEMO_SERVICE_A_ID,
    ApiCase,
    jvm_metric_chart_body,
    service_id_filters,
    time_window,
)

CASE_DIR = Path(__file__).resolve().parent


def build_cases(frm_ms: int, to_ms: int) -> list[ApiCase]:
    page = "JVM"
    sec_frm = frm_ms // 1000
    sec_to = to_ms // 1000

    return [
        ApiCase(page, "JVM 指标目录", "POST", "/webapi/metrics/searchAllMetrics", {
            "type1": "应用性能",
            "type2": "JVM指标",
        }, CASE_DIR),
        ApiCase(page, "JVM 线程数", "POST", "/webapi/metrics/exploreMetricByGroupGraph",
                jvm_metric_chart_body(frm_ms, to_ms, "jvm.thread_count"), CASE_DIR),
        ApiCase(page, "JVM Minor GC 次数", "POST", "/webapi/metrics/exploreMetricByGroupGraph",
                jvm_metric_chart_body(frm_ms, to_ms, "jvm.gc.minor_collection_count"), CASE_DIR),
        ApiCase(page, "JVM Major GC 次数", "POST", "/webapi/metrics/exploreMetricByGroupGraph",
                jvm_metric_chart_body(frm_ms, to_ms, "jvm.gc.major_collection_count"), CASE_DIR),
        ApiCase(page, "JVM Minor GC 时间", "POST", "/webapi/metrics/exploreMetricByGroupGraph",
                jvm_metric_chart_body(frm_ms, to_ms, "jvm.gc.minor_collection_time"), CASE_DIR),
        ApiCase(page, "JVM 堆内存 used", "POST", "/webapi/metrics/exploreMetricByGroupGraph",
                jvm_metric_chart_body(frm_ms, to_ms, "jvm.memory.heap.used"), CASE_DIR),
        ApiCase(page, "JVM lastTags", "POST", "/webapi/metrics/lastLastTagValues", {
            **time_window(frm_ms, to_ms),
            "metrics": ["jvm.thread_count"],
            "by": ["serviceInstance"],
            "from": service_id_filters(DEMO_SERVICE_A_ID),
        }, CASE_DIR),
        ApiCase(page, "JVM v1 metric series", "POST", "/webapi/api/v1/apm/metric/series", {
            "metric": "jvm.thread_count",
            "start": sec_frm,
            "end": sec_to,
            "from": service_id_filters(DEMO_SERVICE_A_ID),
        }, CASE_DIR),
    ]
