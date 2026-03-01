#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SOURCE_MODE="git"
BASE_COMMIT=""
HEAD_COMMIT=""
APP_CONTAINER="${APP_CONTAINER:-carbosys-app}"
MSA_MANAGER_URL="${MSA_MANAGER_URL:-}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source)
      SOURCE_MODE="$2"
      shift 2
      ;;
    --base)
      BASE_COMMIT="$2"
      shift 2
      ;;
    --head)
      HEAD_COMMIT="$2"
      shift 2
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

cd "$ROOT_DIR"

if [[ -z "$MSA_MANAGER_URL" ]]; then
  for cand in "http://carbosys-app:18030/admin/msa" "http://localhost:18030/admin/msa"; do
    if curl -fsS --max-time 3 "$cand/api/modules" >/dev/null 2>&1; then
      MSA_MANAGER_URL="$cand"
      break
    fi
  done
fi

ARGS=(--source "$SOURCE_MODE")
[[ -n "$BASE_COMMIT" ]] && ARGS+=(--base "$BASE_COMMIT")
[[ -n "$HEAD_COMMIT" ]] && ARGS+=(--head "$HEAD_COMMIT")

mapfile -t MODS < <(bash scripts/ci/detect_changed_modules.sh "${ARGS[@]}" || true)
if [[ ${#MODS[@]} -eq 0 ]]; then
  echo "No changed modules detected (source=$SOURCE_MODE)"
  exit 0
fi

printf '%s\n' "${MODS[@]}" > .changed_modules
paste -sd, .changed_modules > .changed_modules.csv

echo "Detected modules: $(cat .changed_modules.csv)"
APP_CONTAINER="$APP_CONTAINER" bash scripts/ci/build_modules_in_container.sh "${MODS[@]}"
MSA_MANAGER_URL="$MSA_MANAGER_URL" bash scripts/ci/deploy_modules_via_manager.sh "${MODS[@]}"
