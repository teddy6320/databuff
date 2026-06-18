"""全局拓扑 /appMonitor/globalTopology"""

from __future__ import annotations

from pathlib import Path

from ...common import ApiCase, DEMO_SERVICE_A, time_window


CASE_DIR = Path(__file__).resolve().parent


def build_cases(frm_ms: int, to_ms: int) -> list[ApiCase]:
    page = "全局拓扑"
    tw = time_window(frm_ms, to_ms)
    return [
        ApiCase(page, "拓扑图", "POST", "/webapi/globalTopology/graph", tw, CASE_DIR),
        ApiCase(page, "垂直树", "POST", "/webapi/globalTopology/verticalTree", {**tw, "serviceId": DEMO_SERVICE_A}, CASE_DIR),
        ApiCase(page, "拓扑边 v1", "POST", "/webapi/api/v1/apm/topology/edges", tw, CASE_DIR),
    ]
