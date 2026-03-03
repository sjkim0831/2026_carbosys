#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INTERVAL_SEC="${WATCH_INTERVAL_SEC:-5}"
APP_CONTAINER="${APP_CONTAINER:-carbosys-app}"
MSA_MANAGER_URL="${MSA_MANAGER_URL:-http://carbosys-app:18030/admin/msa}"
LAST_HASH=""

snapshot_hash() {
  find "$ROOT_DIR" -type f \
    ! -path "$ROOT_DIR/.git/*" \
    ! -path "$ROOT_DIR/logs/*" \
    ! -path "$ROOT_DIR/data/*" \
    ! -path "$ROOT_DIR/file/*" \
    ! -path "$ROOT_DIR/module/*/target/*" \
    ! -path "$ROOT_DIR/module/EgovMsaManager/runtime/*" \
    ! -path "$ROOT_DIR/.ops-control/*" \
    -printf '%P|%T@\n' | sort | sha256sum | awk '{print $1}'
}

cd "$ROOT_DIR"

echo "Watching local folder changes at $ROOT_DIR (interval=${INTERVAL_SEC}s)"
while true; do
  CUR_HASH="$(snapshot_hash)"
  if [[ -z "$LAST_HASH" ]]; then
    LAST_HASH="$CUR_HASH"
  elif [[ "$CUR_HASH" != "$LAST_HASH" ]]; then
    LAST_HASH="$CUR_HASH"
    echo "Change detected. Running module build/deploy pipeline..."
    APP_CONTAINER="$APP_CONTAINER" MSA_MANAGER_URL="$MSA_MANAGER_URL" \
      scripts/ci/run_changed_modules_pipeline.sh --source working-tree || true
  fi
  sleep "$INTERVAL_SEC"
done
