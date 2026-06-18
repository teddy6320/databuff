#!/usr/bin/env bash
# 一键发布到内网（地址见 deploy/env.sh）
#
# Optional:
#   SKIP_BUILD=1          跳过镜像构建
#   SKIP_DOCKER_PKG=1     跳过 Docker 部署包
#   SKIP_K8S_PKG=1        跳过 K8s 部署包

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

if [[ "${SKIP_BUILD:-0}" != "1" ]]; then
  "${ROOT}/images/build-images.sh"
fi
if [[ "${SKIP_DOCKER_PKG:-0}" != "1" ]]; then
  "${ROOT}/docker/build-docker.sh"
fi
if [[ "${SKIP_K8S_PKG:-0}" != "1" ]]; then
  "${ROOT}/k8s/build-k8s.sh"
fi
