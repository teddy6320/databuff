"""Wait until ingest has been running for the required warmup duration."""

from __future__ import annotations

import os
import re
import subprocess
import time
from datetime import datetime, timezone
from typing import Optional

_DOCKER_TIME_RE = re.compile(
    r"^(?P<date>\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})"
    r"(?:\.(?P<frac>\d+))?"
    r"Z$"
)


def _parse_docker_started_at(value: str) -> Optional[datetime]:
    raw = value.strip()
    if not raw or raw == "0001-01-01T00:00:00Z":
        return None
    match = _DOCKER_TIME_RE.match(raw)
    if not match:
        return None
    frac = (match.group("frac") or "0")[:6].ljust(6, "0")
    text = f"{match.group('date')}.{frac}+00:00"
    return datetime.fromisoformat(text)


def ingest_started_at(container: str) -> Optional[datetime]:
    """Return UTC start time of the ingest container, or None if unavailable."""
    try:
        result = subprocess.run(
            ["docker", "inspect", container, "--format", "{{.State.StartedAt}}"],
            capture_output=True,
            text=True,
            timeout=10,
            check=False,
        )
    except (OSError, subprocess.SubprocessError):
        return None
    if result.returncode != 0:
        return None
    return _parse_docker_started_at(result.stdout)


def ingest_uptime_seconds(container: str, *, now: Optional[datetime] = None) -> Optional[float]:
    started = ingest_started_at(container)
    if started is None:
        return None
    current = now or datetime.now(timezone.utc)
    return max(0.0, (current - started).total_seconds())


def wait_for_ingest_warmup(min_seconds: int, container: Optional[str] = None) -> tuple[Optional[float], int]:
    """Block until ingest uptime reaches min_seconds.

    Returns (uptime_before_wait, seconds_waited).
    """
    if min_seconds <= 0:
        return ingest_uptime_seconds(container or _default_container()), 0

    name = container or _default_container()
    uptime = ingest_uptime_seconds(name)
    if uptime is None:
        return None, 0

    if uptime >= min_seconds:
        return uptime, 0

    remaining = int(min_seconds - uptime)
    time.sleep(remaining)
    return uptime, remaining


def _default_container() -> str:
    return os.environ.get("TEST_INGEST_CONTAINER", "ingest")
