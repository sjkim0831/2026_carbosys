#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

docker compose --profile ci up -d --build

echo "Jenkins + app stack is up."
echo "Jenkins: http://localhost:18081"
