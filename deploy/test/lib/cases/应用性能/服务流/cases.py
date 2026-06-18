"""服务流 /appMonitor/serviceFlow"""

from __future__ import annotations

from pathlib import Path

from ...common import ApiCase, DEMO_SERVICE_A, DEMO_SERVICE_B, service_body, time_window


CASE_DIR = Path(__file__).resolve().parent


def build_cases(frm_ms: int, to_ms: int) -> list[ApiCase]:
    page = "服务流"
    return [
        ApiCase(page, "服务流入口", "POST", "/webapi/trace/serviceFlowEndpoint", service_body(frm_ms, to_ms), CASE_DIR),
        ApiCase(page, "单服务流", "POST", "/webapi/trace/serviceFlow", service_body(frm_ms, to_ms), CASE_DIR),
        ApiCase(
            page,
            "多服务流",
            "POST",
            "/webapi/trace/multipleServiceFlow",
            {"services": [DEMO_SERVICE_A, DEMO_SERVICE_B], **time_window(frm_ms, to_ms)},
            CASE_DIR,
        ),
        ApiCase(page, "服务流边 v1", "POST", "/webapi/api/v1/apm/serviceFlow/edges", time_window(frm_ms, to_ms), CASE_DIR),
    ]
