#!/usr/bin/env bash
# 仅下载 ingest / web / demo 离线镜像包并导入（databuffhub/* 短名）。
#
# 用法:
#   ./deploy/k8s/download-apm-images.sh
#   FORCE_PULL_IMAGES=1 ./deploy/k8s/download-apm-images.sh

set -euo pipefail

cd /opt 2>/dev/null || cd "${TMPDIR:-/tmp}" 2>/dev/null || true

ROOT="$(cd "$(dirname "$0")" && pwd)"

if [[ -f "${ROOT}/env.sh" ]]; then
  # shellcheck disable=SC1091
  source "${ROOT}/env.sh"
elif [[ -f "${ROOT}/../env.sh" ]]; then
  # shellcheck disable=SC1091
  source "${ROOT}/../env.sh"
fi

if [[ -f "${ROOT}/VERSION" ]]; then
  APM_VERSION="$(tr -d '[:space:]' <"${ROOT}/VERSION")"
  export APM_VERSION
fi
if declare -F apm_refresh_image_refs >/dev/null 2>&1; then
  apm_refresh_image_refs
fi
if declare -F apm_refresh_image_pkg_bases >/dev/null 2>&1; then
  apm_refresh_image_pkg_bases
fi

if [[ -f "${ROOT}/scripts/image-pkg.sh" ]]; then
  # shellcheck source=scripts/image-pkg.sh
  source "${ROOT}/scripts/image-pkg.sh"
else
  # shellcheck source=../docker/scripts/image-pkg.sh
  source "${ROOT}/../docker/scripts/image-pkg.sh"
fi

export FORCE_PULL_IMAGES=1
echo "[download-apm-images] 强制更新 ingest / web / demo 镜像"
load_k8s_apm_images
echo "[download-apm-images] 全部完成"
