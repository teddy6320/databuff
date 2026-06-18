#!/usr/bin/env bash
# 打 K8s 部署包（主栈 + demo）并 SCP 上传到 databuff-site（APM_PKG_REMOTE_DIR）。
# 镜像构建与 push 请使用 build-images.sh。
#
# Usage:
#   ./deploy/k8s/build-k8s.sh
#
# Optional:
#   SKIP_PKG_UPLOAD=1   跳过部署包上传
#   DEMO_OTLP_ENDPOINT=http://ai-apm-ingest:4318
#
# 下载地址见 deploy/env.sh，build 时写入 install 脚本。

set -euo pipefail

K8S_ROOT="$(cd "$(dirname "$0")" && pwd)"
export APM_BUILD_DIST="${APM_BUILD_DIST:-${TMPDIR:-/tmp}/databuff-apm-k8s-dist}"
source "${K8S_ROOT}/../images/scripts/lib.sh"
log_pkg_publish_targets

RELEASE_VERSION="$(resolve_release_version)"
PKG_NAME="databuff-ai-apm-k8s-${RELEASE_VERSION}"
DEMO_PKG_NAME="databuff-apm-demo-k8s-${RELEASE_VERSION}"
STAGE_PARENT="$(mktemp -d "${TMPDIR:-/tmp}/${PKG_NAME}.stage.XXXXXX")"
STAGE_DIR="${STAGE_PARENT}/${PKG_NAME}"
DEMO_STAGE="${STAGE_PARENT}/${DEMO_PKG_NAME}"
ARCHIVE="${APM_BUILD_DIST}/${PKG_NAME}.tar.gz"
DEMO_ARCHIVE="${APM_BUILD_DIST}/${DEMO_PKG_NAME}.tar.gz"
MANIFEST_SRC="${APM_K8S_SRC}/manifests"
DEMO_K8S_SRC="${APM_K8S_DEMO_SRC}/demo-seeder.yaml"
INSTALL_SCRIPT="${APM_K8S_SRC}/ai-apm-k8s-install.sh"
DEMO_INSTALL_SCRIPT="${APM_K8S_SRC}/ai-apm-demo-k8s-install.sh"
DOWNLOAD_SCRIPT="${APM_K8S_SRC}/ai-apm-k8s-download-images.sh"
DOWNLOAD_APM_SCRIPT="${APM_K8S_SRC}/ai-apm-k8s-download-apm-images.sh"
DIST_INSTALL="${APM_BUILD_DIST}/ai-apm-k8s-install.sh"
DIST_DEMO_INSTALL="${APM_BUILD_DIST}/ai-apm-demo-k8s-install.sh"
DIST_DOWNLOAD="${APM_BUILD_DIST}/ai-apm-k8s-download-images.sh"
DIST_DOWNLOAD_APM="${APM_BUILD_DIST}/ai-apm-k8s-download-apm-images.sh"
DIST_ENV="${APM_BUILD_DIST}/env.sh"
DIST_IMAGE_PKG="${APM_BUILD_DIST}/image-pkg.sh"

cleanup_stage() {
  rm -rf "$STAGE_PARENT"
}
trap cleanup_stage EXIT

mkdir -p "$STAGE_DIR" "$DEMO_STAGE" "$APM_BUILD_DIST"

stage_main_package() {
  mkdir -p "${STAGE_DIR}/manifests" "${STAGE_DIR}/sql" "${STAGE_DIR}/scripts"
  cp -R "${MANIFEST_SRC}/." "${STAGE_DIR}/manifests/"
  cp -R "${APM_K8S_SRC}/scripts/." "${STAGE_DIR}/scripts/"
  cp -f "${APM_COMMON_SRC}/scripts/doris-be-wait.sh" "${STAGE_DIR}/scripts/doris-be-wait.sh"
  stage_runtime_env_sh "${STAGE_DIR}/env.sh" "$RELEASE_VERSION"
  cp -f "${APM_COMMON_SRC}/sql/databuff.sql" "${STAGE_DIR}/sql/databuff.sql"
  copy_install_script "$INSTALL_SCRIPT" "${STAGE_DIR}/install.sh"
  cp -f "${APM_K8S_SRC}/start.sh" "${STAGE_DIR}/start.sh"
  cp -f "${APM_K8S_SRC}/stop.sh" "${STAGE_DIR}/stop.sh"
  cp -f "${APM_K8S_SRC}/download-images.sh" "${STAGE_DIR}/download-images.sh"
  cp -f "${APM_K8S_SRC}/download-apm-images.sh" "${STAGE_DIR}/download-apm-images.sh"
  cp -f "${APM_DOCKER_SRC}/scripts/image-pkg.sh" "${STAGE_DIR}/scripts/image-pkg.sh"
  patch_pkg_urls_in_file "${STAGE_DIR}/scripts/lib.sh"
  chmod +x \
    "${STAGE_DIR}/install.sh" \
    "${STAGE_DIR}/start.sh" \
    "${STAGE_DIR}/stop.sh" \
    "${STAGE_DIR}/download-images.sh" \
    "${STAGE_DIR}/download-apm-images.sh" \
    "${STAGE_DIR}/scripts/image-pkg.sh"
  printf '%s\n' "$RELEASE_VERSION" >"${STAGE_DIR}/VERSION"
  printf '%s\n' "$(project_doris_version)" >"${STAGE_DIR}/DORIS_VERSION"
}

stage_demo_k8s_package() {
  echo "[build-k8s] staging demo k8s package ${DEMO_STAGE} ..."
  render_demo_k8s_manifest \
    "$DEMO_K8S_SRC" \
    "${DEMO_STAGE}/demo-seeder.yaml" \
    "$RELEASE_VERSION" \
    "$(runtime_image_namespace)" \
    "${DEMO_OTLP_ENDPOINT:-http://ai-apm-ingest:4318}"
  copy_install_script "$DEMO_INSTALL_SCRIPT" "${DEMO_STAGE}/install.sh"
  stage_runtime_env_sh "${DEMO_STAGE}/env.sh" "$RELEASE_VERSION"
  printf '%s\n' "$RELEASE_VERSION" >"${DEMO_STAGE}/VERSION"
}

stage_main_package
stage_demo_k8s_package
create_tarball "$STAGE_DIR" "$ARCHIVE"
create_tarball "$DEMO_STAGE" "$DEMO_ARCHIVE"
copy_install_script "$INSTALL_SCRIPT" "$DIST_INSTALL"
copy_install_script "$DEMO_INSTALL_SCRIPT" "$DIST_DEMO_INSTALL"
copy_install_script "$DOWNLOAD_SCRIPT" "$DIST_DOWNLOAD"
copy_install_script "$DOWNLOAD_APM_SCRIPT" "$DIST_DOWNLOAD_APM"
cp -f "${APM_DEPLOY_ROOT}/env.sh" "$DIST_ENV"
copy_install_script "${APM_DOCKER_SRC}/scripts/image-pkg.sh" "$DIST_IMAGE_PKG"
publish_version_k8s_pkg "$RELEASE_VERSION" \
  "$ARCHIVE" \
  "$DEMO_ARCHIVE"
publish_root_pkg \
  "$DIST_INSTALL" \
  "$DIST_DEMO_INSTALL" \
  "$DIST_DOWNLOAD" \
  "$DIST_DOWNLOAD_APM" \
  "$DIST_ENV" \
  "$DIST_IMAGE_PKG"
publish_version_manifest "$RELEASE_VERSION"

cat <<EOF

[build-k8s] done
  Version  : ${RELEASE_VERSION}
  Archive  : ${ARCHIVE}
  Demo     : ${DEMO_ARCHIVE}
  Install  : ${DIST_INSTALL}
             ${DIST_DEMO_INSTALL}
             ${DIST_DOWNLOAD}
             ${DIST_DOWNLOAD_APM}
  Pkg URL  : $(pkg_base_url)

Download / update images:
  Full stack:
    curl -fsSL $(pkg_base_url)/ai-apm-k8s-download-images.sh | bash
  APM only (ingest + web + demo):
    curl -fsSL $(pkg_base_url)/ai-apm-k8s-download-apm-images.sh | bash

Target cluster install:
  curl -fsSL $(pkg_base_url)/ai-apm-k8s-install.sh | bash
  curl -fsSL $(pkg_base_url)/ai-apm-demo-k8s-install.sh | bash

EOF
