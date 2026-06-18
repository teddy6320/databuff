"""Demo seed timing helpers for integration test query windows."""

from __future__ import annotations

import os
import time

# Demo seeder default interval (see DemoOrderSeeder SEED_INTERVAL_SECONDS)
SEED_INTERVAL_SECONDS = int(os.environ.get("SEED_INTERVAL_SECONDS", "30"))
JVM_METRIC_INTERVAL_SECONDS = int(os.environ.get("JVM_METRIC_INTERVAL_SECONDS", "60"))

# User requirement: service up >= 4 min, query last 5 min
MIN_WARMUP_SECONDS = int(os.environ.get("TEST_MIN_WARMUP_SECONDS", "240"))
QUERY_WINDOW_MS = int(os.environ.get("TEST_QUERY_WINDOW_MS", "300000"))

# Portal / Vuex globalTime: end bound is (now - 1 min) floored to a whole minute.
ONE_MINUTE_MS = 60_000


def aligned_query_window(window_ms: int = QUERY_WINDOW_MS, now_ms: int | None = None) -> tuple[int, int]:
    """Return [from_ms, to_ms) on minute boundaries (seconds and ms are zero).

    Matches PortalTimeParser.portalEndNow() and frontend globalTime select mode.
    """
    if now_ms is None:
        now_ms = int(time.time() * 1000)
    to_ms = ((now_ms - ONE_MINUTE_MS) // ONE_MINUTE_MS) * ONE_MINUTE_MS
    frm_ms = to_ms - window_ms
    return frm_ms, to_ms


def trace_batch_bounds(window_ms: int = QUERY_WINDOW_MS) -> tuple[int, int]:
    """Expected trace batch count for a query window given demo seed interval."""
    window_sec = max(1, window_ms // 1000)
    low = max(1, window_sec // SEED_INTERVAL_SECONDS - 1)
    high = (window_sec + SEED_INTERVAL_SECONDS - 1) // SEED_INTERVAL_SECONDS + 1
    return low, high


def jvm_metric_batch_bounds(window_ms: int = QUERY_WINDOW_MS) -> tuple[int, int]:
    """Expected JVM metric batch count for a query window."""
    window_sec = max(1, window_ms // 1000)
    low = max(1, window_sec // JVM_METRIC_INTERVAL_SECONDS - 1)
    high = (window_sec + JVM_METRIC_INTERVAL_SECONDS - 1) // JVM_METRIC_INTERVAL_SECONDS + 1
    return low, high


def scaled_range(per_trace: int, window_ms: int = QUERY_WINDOW_MS) -> dict[str, list[int]]:
    low, high = trace_batch_bounds(window_ms)
    return {"$range": [per_trace * low, per_trace * high]}
