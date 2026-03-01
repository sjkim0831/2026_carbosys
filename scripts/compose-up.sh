#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROFILE="${1:-app}"
BUILD_FLAG="${2:-}"

cd "$ROOT_DIR"

CMD=(docker compose)
if [[ "$PROFILE" == "ci" ]]; then
  CMD+=(--profile ci)
fi
CMD+=(up -d)
if [[ "$BUILD_FLAG" == "--build" ]]; then
  CMD+=(--build)
fi

"${CMD[@]}"
echo "compose up completed (profile=$PROFILE)"
