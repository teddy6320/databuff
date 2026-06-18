#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

# shellcheck source=scripts/compose-env.sh
. "${ROOT}/scripts/compose-env.sh"
compose_down
echo "[stop] local stack stopped"
