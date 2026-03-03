#!/usr/bin/env bash
set -euo pipefail

if [[ $# -eq 0 ]]; then
  echo "No modules to build"
  exit 0
fi

APP_CONTAINER="${APP_CONTAINER:-carbosys-app}"
PROJECT_PATH_IN_CONTAINER="${PROJECT_PATH_IN_CONTAINER:-/opt/carbosys}"
MAVEN_OPTS_EXTRA="${MAVEN_OPTS_EXTRA:--DskipTests package}"

for mod in "$@"; do
  echo "[BUILD] $mod"
  docker exec "$APP_CONTAINER" sh -lc \
    "cd '$PROJECT_PATH_IN_CONTAINER' && mvn -pl module/$mod -am $MAVEN_OPTS_EXTRA"
done
