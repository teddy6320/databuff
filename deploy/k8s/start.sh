#!/usr/bin/env bash
# DataBuff AI APM K8s 启动（按顺序拉起服务，不卸载已有资源）
#
# 用法: ./start.sh
#
# 启动顺序:
#   1. configmap → zookeeper + doris（并行）
#   2. Doris SQL 初始化（库表不存在时）
#   3. ingest + web（并行等待 Ready）
#
# 环境变量: 与 install.sh 相同，另支持 APM_INSTALL_DIR 指向部署包目录

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=scripts/lib.sh
. "${ROOT}/scripts/lib.sh"

main() {
  export APM_LOG_PREFIX=start
  deploy_stack
  if [[ "${START_SKIP_SUMMARY:-0}" != "1" ]]; then
    print_start_summary
  fi
}

main "$@"
