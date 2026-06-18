#!/usr/bin/env bash
set -e

_script_dir="$(cd "$(dirname "$0")" && pwd)"
if [ -n "${APM_ROOT:-}" ]; then
  ROOT="$APM_ROOT"
elif [ -f "${_script_dir}/.env" ] || [ -f "${_script_dir}/VERSION" ]; then
  ROOT="$_script_dir"
else
  ROOT="$(cd "${_script_dir}/.." && pwd)"
fi

if [ -f "${ROOT}/env.sh" ]; then
  set -a
  # shellcheck disable=SC1091
  . "${ROOT}/env.sh"
  set +a
elif [ -f "${ROOT}/../env.sh" ]; then
  set -a
  # shellcheck disable=SC1091
  . "${ROOT}/../env.sh"
  set +a
fi
if [ -f "${ROOT}/VERSION" ]; then
  APM_VERSION="$(tr -d '[:space:]' <"${ROOT}/VERSION")"
  export APM_VERSION
fi
if declare -F apm_refresh_image_refs >/dev/null 2>&1; then
  apm_refresh_image_refs
fi
if declare -F apm_refresh_image_pkg_bases >/dev/null 2>&1; then
  apm_refresh_image_pkg_bases
fi

: "${APM_VERSION:?set APM_VERSION in env.sh}"
: "${APM_INGEST_IMAGE:?set APM_INGEST_IMAGE in env.sh}"
: "${APM_WEB_IMAGE:?set APM_WEB_IMAGE in env.sh}"
: "${DORIS_FE_IMAGE:=apache/doris:fe-4.1.1}"
: "${DORIS_BE_IMAGE:=apache/doris:be-4.1.1}"

# shellcheck source=image-pkg.sh
. "${_script_dir}/image-pkg.sh"

load_docker_stack_images
