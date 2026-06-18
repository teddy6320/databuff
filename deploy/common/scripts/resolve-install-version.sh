#!/usr/bin/env bash
# 远程 install / download 脚本共用的版本解析。
# 使用前需设置 PKG_BASE；可选 BUILTIN_APM_VERSION（构建时写入的内置回退版本）。

resolve_apm_install_version() {
  if [[ -n "${APM_VERSION:-}" ]]; then
    printf '%s\n' "$APM_VERSION"
    return 0
  fi

  local latest=""
  if [[ -n "${PKG_BASE:-}" ]] && command -v curl >/dev/null 2>&1; then
    latest="$(curl -fsSL "${PKG_BASE%/}/VERSION" 2>/dev/null | head -n1 | tr -d '[:space:]')" || true
  fi
  if [[ -n "$latest" ]]; then
    printf '%s\n' "$latest"
    return 0
  fi

  if [[ -n "${BUILTIN_APM_VERSION:-}" ]]; then
    printf '%s\n' "$BUILTIN_APM_VERSION"
    return 0
  fi

  echo "[install] ERROR: cannot resolve version (set APM_VERSION, use --version, or ensure ${PKG_BASE}/VERSION exists)" >&2
  return 1
}

# install / download 脚本解析版本后调用，设置镜像包 HTTP 目录。
export_apm_pkg_download_env() {
  export APM_PKG_BASE="${APM_PKG_BASE:-${PKG_BASE:?PKG_BASE required}}"
  export APM_IMAGES_PKG_BASE="${APM_IMAGES_PKG_BASE:-${APM_PKG_BASE%/}/${APM_VERSION}/images}"
  export APM_INFRA_IMAGES_PKG_BASE="${APM_INFRA_IMAGES_PKG_BASE:-${APM_PKG_BASE%/}/infra/images}"
}

apm_docker_pkg_download_url() {
  local pkg_name="$1"
  printf '%s\n' "${PKG_BASE%/}/${APM_VERSION}/docker/${pkg_name}"
}

apm_k8s_pkg_download_url() {
  local pkg_name="$1"
  printf '%s\n' "${PKG_BASE%/}/${APM_VERSION}/k8s/${pkg_name}"
}
