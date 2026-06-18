#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-databuff-ai-apm-demo}"

if [ -f "${ROOT}/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  . "${ROOT}/.env"
  set +a
elif [ -f "${ROOT}/env.sh" ]; then
  set -a
  # shellcheck disable=SC1091
  . "${ROOT}/env.sh"
  set +a
elif [ -f "${ROOT}/../../env.sh" ]; then
  set -a
  # shellcheck disable=SC1091
  . "${ROOT}/../../env.sh"
  set +a
fi
if [ -f "${ROOT}/VERSION" ]; then
  export APM_VERSION
  APM_VERSION="$(tr -d '[:space:]' <"${ROOT}/VERSION")"
fi
if declare -F apm_refresh_image_refs >/dev/null 2>&1; then
  apm_refresh_image_refs
fi

if docker compose version >/dev/null 2>&1; then
  docker compose down --remove-orphans
elif command -v docker-compose >/dev/null 2>&1; then
  docker-compose down --remove-orphans
else
  echo "[stop] docker compose not found" >&2
  exit 1
fi

echo "[stop] demo seeder stopped"
