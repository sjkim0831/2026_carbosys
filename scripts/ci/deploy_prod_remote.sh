#!/usr/bin/env bash
set -euo pipefail

if [[ $# -eq 0 ]]; then
  echo "No modules to deploy to prod"
  exit 0
fi

if [[ -z "${PROD_SSH_HOST:-}" || -z "${PROD_SSH_USER:-}" ]]; then
  echo "PROD_SSH_HOST and PROD_SSH_USER are required" >&2
  exit 1
fi
if [[ ! -f "${PROD_SSH_KEY_PATH:-}" ]]; then
  echo "SSH key not found: ${PROD_SSH_KEY_PATH:-}" >&2
  exit 1
fi

PROD_SSH_PORT="${PROD_SSH_PORT:-22}"
PROD_DEPLOY_ROOT="${PROD_DEPLOY_ROOT:-/opt/carbosys}"
PROD_MSA_MANAGER_URL="${PROD_MSA_MANAGER_URL:-}"
PROD_DEPLOY_ENGINE="${PROD_DEPLOY_ENGINE:-msa-api}"  # msa-api | direct-restart
PROD_APP_CONTAINER="${PROD_APP_CONTAINER:-carbosys-app}"

SSH_OPTS=(-i "$PROD_SSH_KEY_PATH" -p "$PROD_SSH_PORT" -o StrictHostKeyChecking=no)

for mod in "$@"; do
  jar="module/$mod/target/$mod.jar"
  if [[ ! -f "$jar" ]]; then
    echo "Jar not found: $jar" >&2
    exit 1
  fi

  remote_dir="$PROD_DEPLOY_ROOT/module/$mod/target"
  echo "[UPLOAD][PROD] $mod -> $PROD_SSH_USER@$PROD_SSH_HOST:$remote_dir"
  ssh "${SSH_OPTS[@]}" "$PROD_SSH_USER@$PROD_SSH_HOST" "mkdir -p '$remote_dir'"
  scp "${SSH_OPTS[@]}" "$jar" "$PROD_SSH_USER@$PROD_SSH_HOST:$remote_dir/$mod.jar"
done

if [[ "$PROD_DEPLOY_ENGINE" == "msa-api" ]]; then
  if [[ -z "$PROD_MSA_MANAGER_URL" ]]; then
    echo "PROD_MSA_MANAGER_URL is required when PROD_DEPLOY_ENGINE=msa-api" >&2
    exit 1
  fi
  for mod in "$@"; do
    endpoint="deploy-zerodowntime"
    if [[ "$mod" == "EgovMsaManager" ]]; then
      endpoint="deploy-restart"
    fi
    echo "[DEPLOY][PROD][MSA-API] $mod via $PROD_MSA_MANAGER_URL/api/modules/$mod/$endpoint"
    resp="$(curl -fsS -X POST "$PROD_MSA_MANAGER_URL/api/modules/$mod/$endpoint")"
    status="$(echo "$resp" | jq -r '.status // "error"')"
    if [[ "$status" != "ok" ]]; then
      echo "Prod deploy API failed for $mod: $resp" >&2
      exit 1
    fi
  done
  exit 0
fi

if [[ "$PROD_DEPLOY_ENGINE" != "direct-restart" ]]; then
  echo "Unsupported PROD_DEPLOY_ENGINE: $PROD_DEPLOY_ENGINE" >&2
  exit 1
fi

remote_script="$(mktemp /tmp/prod-direct-restart.XXXXXX.sh)"
cat > "$remote_script" <<'EOS'
#!/usr/bin/env bash
set -euo pipefail

ROOT="$1"
CONTAINER="$2"
shift 2

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
  ' "$ROOT/msa-ports.yml"
}

for mod in "$@"; do
  port="$(module_port "$mod")"
  if [[ -z "$port" ]]; then
    echo "[DIRECT-RESTART] port not found in msa-ports.yml: $mod" >&2
    exit 1
  fi

  host_jar="$ROOT/module/$mod/target/$mod.jar"
  if [[ ! -f "$host_jar" ]]; then
    echo "[DIRECT-RESTART] jar missing on host: $host_jar" >&2
    exit 1
  fi

  cont_src="/opt/carbosys/module/$mod/target/$mod.jar"
  cont_dst="/opt/carbosys/$mod.jar"
  log_file="/opt/carbosys/logs/$mod.log"

  echo "[DIRECT-RESTART] $mod port=$port"
  docker exec "$CONTAINER" sh -lc "test -f '$cont_src'"
  docker exec "$CONTAINER" sh -lc "cp '$cont_src' '$cont_dst'"
  docker exec "$CONTAINER" sh -lc "pids=\$(pgrep -f '/opt/carbosys/$mod.jar' || true); if [ -n \"\$pids\" ]; then kill -15 \$pids; fi"
  sleep 2
  docker exec "$CONTAINER" sh -lc "nohup java -Xms256m -Xmx512m -jar '$cont_dst' --server.port=$port > '$log_file' 2>&1 &"
done
EOS

chmod +x "$remote_script"
remote_path="/tmp/prod-direct-restart.sh"
scp "${SSH_OPTS[@]}" "$remote_script" "$PROD_SSH_USER@$PROD_SSH_HOST:$remote_path"
ssh "${SSH_OPTS[@]}" "$PROD_SSH_USER@$PROD_SSH_HOST" "bash '$remote_path' '$PROD_DEPLOY_ROOT' '$PROD_APP_CONTAINER' $*; rm -f '$remote_path'"
rm -f "$remote_script"
echo "[DEPLOY][PROD][DIRECT-RESTART] completed"
