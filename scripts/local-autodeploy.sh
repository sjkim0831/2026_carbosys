#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WATCH_INTERVAL_SEC="${WATCH_INTERVAL_SEC:-5}"
APP_CONTAINER="${APP_CONTAINER:-carbosys-app}"
MSA_MANAGER_URL="${MSA_MANAGER_URL:-http://localhost:18030/admin/msa}"
LOCAL_AUTODEPLOY_AUTO_UP="${LOCAL_AUTODEPLOY_AUTO_UP:-1}"
cd "$ROOT_DIR"

if ! curl -fsS --max-time 3 "$MSA_MANAGER_URL/api/modules" >/dev/null 2>&1; then
  if [[ "$LOCAL_AUTODEPLOY_AUTO_UP" == "1" ]]; then
    echo "MSA Manager is not reachable. Starting app stack..."
    docker compose up -d
    for _ in $(seq 1 30); do
      if curl -fsS --max-time 3 "$MSA_MANAGER_URL/api/modules" >/dev/null 2>&1; then
        break
      fi
      sleep 2
    done
  fi
fi

if ! curl -fsS --max-time 3 "$MSA_MANAGER_URL/api/modules" >/dev/null 2>&1; then
  echo "MSA Manager is still unreachable at $MSA_MANAGER_URL" >&2
  echo "Set MSA_MANAGER_URL correctly or run docker compose first." >&2
  exit 1
fi

WATCH_INTERVAL_SEC="$WATCH_INTERVAL_SEC" APP_CONTAINER="$APP_CONTAINER" MSA_MANAGER_URL="$MSA_MANAGER_URL" \
  bash scripts/ci/watch_local_changes.sh
