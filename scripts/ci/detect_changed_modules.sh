#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PORT_FILE="$ROOT_DIR/msa-ports.yml"

if [[ ! -f "$PORT_FILE" ]]; then
  echo "msa-ports.yml not found: $PORT_FILE" >&2
  exit 1
fi

SOURCE_MODE="git"
BASE_COMMIT="${GIT_PREVIOUS_SUCCESSFUL_COMMIT:-}"
HEAD_COMMIT="${GIT_COMMIT:-HEAD}"

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

mapfile -t DEPLOYABLE < <(awk '
  $1 == "ports:" { in_ports = 1; next }
  in_ports && $0 ~ /^[^[:space:]]/ { in_ports = 0 }
  in_ports && $1 ~ /^[A-Za-z0-9]+:/ {
    key = $1
    sub(":", "", key)
    print key
  }
' "$PORT_FILE" | grep -Ev '^(EurekaServer|ConfigServer|GatewayServer)$' || true)

if [[ ${#DEPLOYABLE[@]} -eq 0 ]]; then
  exit 0
fi

DEPLOYABLE_SET=" $(printf '%s ' "${DEPLOYABLE[@]}")"

collect_git_changed_files() {
  local base="$1"
  local head="$2"
  if [[ -z "$base" ]]; then
    if git rev-parse --verify --quiet "${head}~1" >/dev/null; then
      base="${head}~1"
    else
      base="$(git rev-list --max-parents=0 "$head" | tail -n 1)"
    fi
  fi
  git diff --name-only "$base" "$head"
}

collect_working_tree_changed_files() {
  {
    git diff --name-only
    git diff --name-only --cached
    git ls-files --others --exclude-standard
  } | awk 'NF' | sort -u
}

case "$SOURCE_MODE" in
  git)
    mapfile -t CHANGED_FILES < <(collect_git_changed_files "$BASE_COMMIT" "$HEAD_COMMIT")
    ;;
  working-tree)
    mapfile -t CHANGED_FILES < <(collect_working_tree_changed_files)
    ;;
  *)
    echo "Unsupported source mode: $SOURCE_MODE" >&2
    exit 1
    ;;
esac

if [[ ${#CHANGED_FILES[@]} -eq 0 ]]; then
  exit 0
fi

GLOBAL_CHANGED=0
{
  for f in "${CHANGED_FILES[@]}"; do
    case "$f" in
      pom.xml|Dockerfile|entrypoint.sh|docker-compose.yml|msa-ports.yml|msa-mappings.yml)
        GLOBAL_CHANGED=1
        ;;
    esac

    if [[ "$f" =~ ^module/([^/]+)/ ]]; then
      name="${BASH_REMATCH[1]}"
      if [[ "$DEPLOYABLE_SET" == *" $name "* ]]; then
        echo "$name"
      fi
    fi
  done

  if [[ $GLOBAL_CHANGED -eq 1 ]]; then
    printf '%s\n' "${DEPLOYABLE[@]}"
  fi
} | awk 'NF' | sort -u
