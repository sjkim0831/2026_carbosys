#!/usr/bin/env bash
set -euo pipefail

if [[ $# -eq 0 ]]; then
  echo "No modules to deploy"
  exit 0
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PORT_FILE="$ROOT_DIR/msa-ports.yml"
MSA_MANAGER_URL="${MSA_MANAGER_URL:-}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required for deploy status parsing" >&2
  exit 1
fi

if [[ -z "$MSA_MANAGER_URL" ]]; then
  for cand in "http://carbosys-app:18030/admin/msa" "http://localhost:18030/admin/msa"; do
    if curl -fsS --max-time 3 "$cand/api/modules" >/dev/null 2>&1; then
      MSA_MANAGER_URL="$cand"
      break
    fi
  done
fi

if [[ -z "$MSA_MANAGER_URL" ]]; then
  echo "MSA_MANAGER_URL is not reachable. Set MSA_MANAGER_URL explicitly." >&2
  exit 1
fi

module_port() {
  local module="$1"
  awk -v key="$module" '
    $1 == "ports:" { in_ports = 1; next }
    in_ports && $0 ~ /^[^[:space:]]/ { in_ports = 0 }
    in_ports {
      gsub(":", "", $1)
      if ($1 == key) {
        print $2
        exit
      }
    }
  ' "$PORT_FILE"
}

wait_running() {
  local module="$1"
  local retries=50
  while (( retries > 0 )); do
    status="$(curl -fsS "$MSA_MANAGER_URL/api/modules" | jq -r ".[] | select(.id == \"$module\") | .status" || true)"
    if [[ "$status" == "running" ]]; then
      return 0
    fi
    sleep 2
    retries=$((retries - 1))
  done
  return 1
}

for mod in "$@"; do
  echo "[DEPLOY] $mod"

  endpoint="deploy-zerodowntime"

  resp="$(curl -fsS -X POST "$MSA_MANAGER_URL/api/modules/$mod/$endpoint")"
  status="$(echo "$resp" | jq -r '.status // "error"')"

  if [[ "$status" != "ok" ]]; then
    echo "Deploy API failed for $mod: $resp" >&2
    exit 1
  fi

  if ! wait_running "$mod"; then
    echo "Module did not become running: $mod" >&2
    exit 1
  fi

  port="$(module_port "$mod")"
  if [[ -n "$port" ]]; then
    echo "[DEPLOY] $mod running on port $port"
  fi
done
