"""数据库 /appMonitor/database"""

from __future__ import annotations

from pathlib import Path

from ...common import ApiCase, time_window


CASE_DIR = Path(__file__).resolve().parent


def build_cases(frm_ms: int, to_ms: int) -> list[ApiCase]:
    page = "数据库"
    tw = time_window(frm_ms, to_ms)
    return [
        ApiCase(page, "数据库列表", "POST", "/webapi/service/dbList", tw, CASE_DIR),
        ApiCase(page, "依赖关系列表", "POST", "/webapi/service/relationList", tw, CASE_DIR),
    ]
