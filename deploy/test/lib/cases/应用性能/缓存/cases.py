"""缓存 /appMonitor/cache"""

from __future__ import annotations

from pathlib import Path

from ...common import ApiCase, time_window


CASE_DIR = Path(__file__).resolve().parent


def build_cases(frm_ms: int, to_ms: int) -> list[ApiCase]:
    page = "缓存"
    return [
        ApiCase(page, "缓存列表", "POST", "/webapi/service/cacheList", time_window(frm_ms, to_ms), CASE_DIR),
    ]
