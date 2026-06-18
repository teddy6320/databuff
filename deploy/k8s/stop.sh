#!/usr/bin/env bash
# DataBuff AI APM K8s 停止（缩容至 0，保留 manifests / ConfigMap / Service）
#
# 用法: ./stop.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=scripts/lib.sh
. "${ROOT}/scripts/lib.sh"

main() {
  export APM_LOG_PREFIX=stop
  ensure_kube_access
  stop_services
  log "服务已停止"
}

main "$@"
