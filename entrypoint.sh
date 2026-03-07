#!/bin/bash

set -e
APP_HOME="${APP_HOME:-/opt/app}"

echo "Starting Carbosys MSA Services..."

# JVM memory defaults (override via env if needed)
EUREKA_XMS="${EUREKA_XMS:-64m}"
EUREKA_XMX="${EUREKA_XMX:-128m}"
CONFIG_XMS="${CONFIG_XMS:-64m}"
CONFIG_XMX="${CONFIG_XMX:-128m}"
GATEWAY_XMS="${GATEWAY_XMS:-128m}"
GATEWAY_XMX="${GATEWAY_XMX:-256m}"
EGOV_HOME_XMS="${EGOV_HOME_XMS:-256m}"
EGOV_HOME_XMX="${EGOV_HOME_XMX:-512m}"

# CUBRID wait disabled by request.
# Wait loop intentionally skipped.
echo "Skipping CUBRID readiness wait."

# Start core services (Eureka/Config/EgovHome/Manager first)
echo "Starting EurekaServer..."
java -Xms"${EUREKA_XMS}" -Xmx"${EUREKA_XMX}" -jar "${APP_HOME}/EurekaServer.jar" &

echo "Starting ConfigServer..."
java -Xms"${CONFIG_XMS}" -Xmx"${CONFIG_XMX}" -jar "${APP_HOME}/ConfigServer.jar" &

# Start Gateway/EgovHome only after Config is ready
echo "Waiting for ConfigServer (8888) readiness before starting Gateway and EgovHome..."
CFG_MAX_RETRIES="${CFG_MAX_RETRIES:-120}"
CFG_SLEEP_SEC="${CFG_RETRY_INTERVAL_SEC:-2}"
CFG_COUNT=0
while true; do
  CONFIG_CODE="$(curl -sS -o /dev/null -w "%{http_code}" --max-time 2 http://localhost:8888/actuator/health || true)"
  if [ "$CONFIG_CODE" = "200" ]; then
    echo "Config ready: ${CONFIG_CODE}"
    break
  fi
  CFG_COUNT=$((CFG_COUNT + 1))
  if [ "$CFG_COUNT" -ge "$CFG_MAX_RETRIES" ]; then
    echo "Config readiness timeout after $((CFG_MAX_RETRIES * CFG_SLEEP_SEC)) seconds. Starting Gateway anyway..."
    break
  fi
  sleep "$CFG_SLEEP_SEC"
done

echo "Starting GatewayServer..."
java -Xms"${GATEWAY_XMS}" -Xmx"${GATEWAY_XMX}" -jar "${APP_HOME}/GatewayServer.jar" &

echo "Starting EgovHome..."
java -Xms"${EGOV_HOME_XMS}" -Xmx"${EGOV_HOME_XMX}" -jar "${APP_HOME}/EgovHome.jar" --server.port=18000 &

echo "Core services started (Gateway/EgovHome after Config readiness)."
echo "Other modules should be managed via MSA Manager UI."

# Start Business Services (Commented out to allow Manager control)
# echo "Starting business services..."
# java -jar "${APP_HOME}/EgovMain.jar" &
# java -jar "${APP_HOME}/EgovLogin.jar" &
# java -jar "${APP_HOME}/EgovBoard.jar" &
# java -jar "${APP_HOME}/EgovJoin.jar" &
# java -jar "${APP_HOME}/EgovAuthor.jar" &
# java -jar "${APP_HOME}/EgovCmmnCode.jar" &
# java -jar "${APP_HOME}/EgovQuestionnaire.jar" &
# java -jar "${APP_HOME}/EgovSearch.jar" &
# java -jar "${APP_HOME}/EgovMobileId.jar" &
# java -jar "${APP_HOME}/EgovLoginPolicy.jar" &

# Start UI Services (Commented out)
# echo "Starting UI services..."
# java -jar "${APP_HOME}/home3.jar" &
# java -jar "${APP_HOME}/signin.jar" &

echo "All services started!"
echo "Waiting for all processes..."

# Keep container running
tail -f /dev/null
