#!/usr/bin/env bash
set -euo pipefail

TEST_DIR="$(cd "$(dirname "$0")" && pwd)"
TEST_LIB_DIR="${TEST_DIR}/lib"

# 基准环境（录制 expected）：默认 140，查询最近 5 分钟
export TEST_BASE_URL="${TEST_BASE_URL:-${SNAPSHOT_BASE_URL:-http://192.168.50.140:27403}}"
export TEST_QUERY_WINDOW_MS="${TEST_QUERY_WINDOW_MS:-300000}"
export TEST_WARMUP_SECONDS="${TEST_WARMUP_SECONDS:-0}"

echo "[snapshot-expected] base=${TEST_BASE_URL} window=${TEST_QUERY_WINDOW_MS}ms"
python3 "${TEST_LIB_DIR}/snapshot_expected.py" "$@"
