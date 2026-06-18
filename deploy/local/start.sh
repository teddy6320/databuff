#!/usr/bin/env bash
# 本地一键启动：编译 ingest / web / demo → 挂载 JAR 启动 Doris（含初始化 SQL）+ ingest + web + demo
#
# Usage:
#   ./start.sh
#
# Optional:
#   SKIP_BUILD=1          复用已有 Maven 产物
#   START_SKIP_READY=1    不等待 health check

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

chmod +x "${ROOT}/scripts/"*.sh 2>/dev/null || true

# shellcheck source=scripts/lib.sh
. "${ROOT}/scripts/lib.sh"
load_local_env
ensure_openjdk_image

# shellcheck source=scripts/compose-env.sh
. "${ROOT}/scripts/compose-env.sh"
# shellcheck source=../docker/scripts/runtime.sh
. "${ROOT}/../docker/scripts/runtime.sh"

ensure_vm_max_map_count

if [ "${SKIP_BUILD:-0}" != "1" ]; then
  build_ingest_web
  build_demo
fi
verify_ingest_web_jars
verify_demo_jar
sync_all_run_dirs

if doris_has_data; then
  echo "[start] doris data exists, starting all services"
  compose_up \
    "$DORIS_FE_SERVICE" "$DORIS_BE_SERVICE" \
    "$INGEST_SERVICE" "$WEB_SERVICE" "$DEMO_SERVICE"
else
  echo "[start] initializing doris"
  mkdir -p "${ROOT}/data/fe-meta" "${ROOT}/data/be-storage"
  compose_up "$DORIS_FE_SERVICE" "$DORIS_BE_SERVICE"
  "${ROOT}/scripts/init-doris.sh"
  compose_up "$INGEST_SERVICE" "$WEB_SERVICE" "$DEMO_SERVICE"
fi
force_restart_jar_services

if [ "${START_SKIP_READY:-0}" != "1" ]; then
  wait_for_apm_services_ready || echo "[start] services not fully ready yet; see summary below" >&2
fi

if [ "${START_SKIP_SUMMARY:-0}" != "1" ]; then
  print_apm_ready_summary
  echo "[start] demo OTLP target: http://ai-apm-ingest:4318/v1/traces"
  echo "[start] demo logs: docker logs -f ${DEMO_SERVICE}"
fi
