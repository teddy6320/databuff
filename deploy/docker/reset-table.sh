#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

DORIS_SERVICE="${DORIS_SERVICE:-ai-apm-doris-fe}"

echo "WARNING: this will DROP DATABASE databuff and recreate all tables."
read -r -p "Type 'yes' to continue: " confirm
if [[ "$confirm" != "yes" ]]; then
  echo "[reset-table] cancelled"
  exit 0
fi

# shellcheck source=scripts/compose-env.sh
. "${ROOT}/scripts/compose-env.sh"

echo "[reset-table] dropping databuff ..."
compose_cmd exec -T "$DORIS_SERVICE" mysql -h127.0.0.1 -P9030 -uroot \
  -e "DROP DATABASE IF EXISTS databuff;"

echo "[reset-table] re-applying init SQL ..."
"${ROOT}/scripts/init-doris.sh"

echo "[reset-table] done"
