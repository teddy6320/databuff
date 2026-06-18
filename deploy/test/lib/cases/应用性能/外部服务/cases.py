"""外部服务 /appMonitor/external"""

from __future__ import annotations

from pathlib import Path

from ...common import ApiCase, time_window


CASE_DIR = Path(__file__).resolve().parent


def build_cases(frm_ms: int, to_ms: int) -> list[ApiCase]:
    page = "外部服务"
    return [
        ApiCase(page, "外部调用列表", "POST", "/webapi/service/remoteCallList", time_window(frm_ms, to_ms), CASE_DIR),
    ]
