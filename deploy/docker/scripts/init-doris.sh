#!/usr/bin/env bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=compose-env.sh
. "${ROOT}/scripts/compose-env.sh"

DORIS_SERVICE="${DORIS_SERVICE:-ai-apm-doris-fe}"
MAX=120

resolve_init_sql() {
  if [ -f "${ROOT}/sql/databuff.sql" ]; then
    echo "${ROOT}/sql/databuff.sql"
    return 0
  fi
  if [ -f "${ROOT}/../common/sql/databuff.sql" ]; then
    echo "${ROOT}/../common/sql/databuff.sql"
    return 0
  fi
  if [ -n "${DATABUFF_DEPLOY_COMMON:-}" ] && [ -f "${DATABUFF_DEPLOY_COMMON}/sql/databuff.sql" ]; then
    echo "${DATABUFF_DEPLOY_COMMON}/sql/databuff.sql"
    return 0
  fi
  return 1
}

doris_mysql() {
  compose_cmd exec -T "$DORIS_SERVICE" mysql -h127.0.0.1 -P9030 -uroot "$@"
}

source_doris_be_wait() {
  local p
  for p in \
    "${ROOT}/scripts/doris-be-wait.sh" \
    "${ROOT}/../common/scripts/doris-be-wait.sh"; do
    if [ -f "$p" ]; then
      # shellcheck disable=SC1090,SC1091
      . "$p"
      return 0
    fi
  done
  echo "[init-doris] missing doris-be-wait.sh" >&2
  exit 1
}

source_doris_be_wait

echo "[init-doris] waiting FE in ${DORIS_SERVICE}"
i=1
while [ "$i" -le "$MAX" ]; do
  if doris_mysql -e "SELECT 1" >/dev/null 2>&1; then
    break
  fi
  if [ "$i" -eq "$MAX" ]; then
    echo "[init-doris] timeout waiting for Doris FE" >&2
    exit 1
  fi
  sleep 2
  i=$((i + 1))
done

if [ "${SKIP_BE_WAIT:-0}" != "1" ]; then
  wait_for_be_alive "[init-doris]" "$MAX" 3
  wait_for_be_avail_stable "[init-doris]" 120
fi

INIT_SQL="$(resolve_init_sql || true)"
if [ -z "$INIT_SQL" ]; then
  echo "[init-doris] missing databuff.sql" >&2
  exit 1
fi

echo "[init-doris] applying $(basename "$INIT_SQL")"
apply_doris_sql_file "$INIT_SQL"
echo "[init-doris] ok"
