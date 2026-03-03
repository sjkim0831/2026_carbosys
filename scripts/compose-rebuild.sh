#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROFILE="${1:-app}"
cd "$ROOT_DIR"

if [[ "$PROFILE" == "ci" ]]; then
  docker compose --profile ci up -d --build
else
  docker compose up -d --build
fi

echo "compose rebuild up completed (profile=$PROFILE)"
