#!/usr/bin/env python3
"""Record live API responses into expected/*.json (strict snapshot baseline)."""

from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path

LIB_ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(LIB_ROOT))

from cases import build_cases  # noqa: E402
from demo_window import MIN_WARMUP_SECONDS, QUERY_WINDOW_MS, aligned_query_window  # noqa: E402
from ingest_warmup import wait_for_ingest_warmup  # noqa: E402
from run_tests import http_json, login, wait_for_demo_data_in_window  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description="Snapshot API responses to expected JSON files")
    parser.add_argument("--base-url", default=os.environ.get("TEST_BASE_URL", "http://127.0.0.1:27403"))
    parser.add_argument("--username", default=os.environ.get("TEST_USERNAME", "admin"))
    parser.add_argument("--password", default=os.environ.get("TEST_PASSWORD", "Databuff@123"))
    parser.add_argument("--timeout", type=float, default=float(os.environ.get("TEST_TIMEOUT", "60")))
    parser.add_argument("--warmup", type=int, default=int(os.environ.get("TEST_WARMUP_SECONDS", str(MIN_WARMUP_SECONDS))))
    parser.add_argument("--service", default=os.environ.get("TEST_DEMO_SERVICE", "service-a"))
    args = parser.parse_args()

    warmup = 0 if args.warmup <= 0 else args.warmup
    base = args.base_url.rstrip("/")

    print(f"[snapshot] login {base} ...")
    token = login(base, args.username, args.password, args.timeout)
    wait_for_ingest_warmup(warmup)

    frm_ms, to_ms = aligned_query_window()
    wait_for_demo_data_in_window(base, token, args.service, frm_ms, to_ms, max(min(warmup, 120), 60))

    cases = build_cases(frm_ms, to_ms)
    print(f"[snapshot] recording {len(cases)} cases, window last {QUERY_WINDOW_MS // 1000}s ...")

    ok = 0
    failed = 0
    for case in cases:
        url = f"{base}{case.path}"
        code, _, payload = http_json(case.method, url, body=case.body, token=token, timeout=args.timeout)
        if code != case.expect_status:
            print(f"[snapshot] SKIP {case.group}/{case.name}: HTTP {code}")
            failed += 1
            continue
        out = case.expected_path
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"[snapshot] wrote {out.relative_to(LIB_ROOT)}")
        ok += 1

    print(f"[snapshot] done {ok} written, {failed} skipped")
    return 0 if failed == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
