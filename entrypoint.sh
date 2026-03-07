#!/bin/bash

set -e

echo "Starting Carbosys MSA Services..."

# JVM memory defaults (override via env if needed)
EUREKA_XMS="${EUREKA_XMS:-64m}"
EUREKA_XMX="${EUREKA_XMX:-128m}"
CONFIG_XMS="${CONFIG_XMS:-64m}"
CONFIG_XMX="${CONFIG_XMX:-128m}"
GATEWAY_XMS="${GATEWAY_XMS:-128m}"
GATEWAY_XMX="${GATEWAY_XMX:-256m}"
MSA_MANAGER_XMS="${MSA_MANAGER_XMS:-128m}"
MSA_MANAGER_XMX="${MSA_MANAGER_XMX:-256m}"
EGOV_HOME_XMS="${EGOV_HOME_XMS:-256m}"
EGOV_HOME_XMX="${EGOV_HOME_XMX:-512m}"

# CUBRID wait disabled by request.
# Wait loop intentionally skipped.
echo "Skipping CUBRID readiness wait."

# Start core services (Eureka/Config/EgovHome/Manager first)
echo "Starting EurekaServer..."
java -Xms"${EUREKA_XMS}" -Xmx"${EUREKA_XMX}" -jar /opt/carbosys/EurekaServer.jar &

echo "Starting ConfigServer..."
java -Xms"${CONFIG_XMS}" -Xmx"${CONFIG_XMX}" -jar /opt/carbosys/ConfigServer.jar &

# Start MSA Manager
echo "Waiting for Eureka (8761) readiness before starting EgovMsaManager..."
EUREKA_MAX_RETRIES="${EUREKA_MAX_RETRIES:-120}"
EUREKA_SLEEP_SEC="${EUREKA_RETRY_INTERVAL_SEC:-2}"
EUREKA_COUNT=0
while true; do
  EUREKA_CODE="$(curl -sS -o /dev/null -w "%{http_code}" --max-time 2 http://localhost:8761/ || true)"
  case "$EUREKA_CODE" in
    200|302|401|403)
      echo "Eureka ready: ${EUREKA_CODE}"
      break
      ;;
  esac
  EUREKA_COUNT=$((EUREKA_COUNT + 1))
  if [ "$EUREKA_COUNT" -ge "$EUREKA_MAX_RETRIES" ]; then
    echo "Eureka readiness timeout after $((EUREKA_MAX_RETRIES * EUREKA_SLEEP_SEC)) seconds. Starting EgovMsaManager anyway..."
    break
  fi
  sleep "$EUREKA_SLEEP_SEC"
done

echo "Starting EgovMsaManager..."
java -Xms"${MSA_MANAGER_XMS}" -Xmx"${MSA_MANAGER_XMX}" -jar /opt/carbosys/EgovMsaManager.jar &

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
java -Xms"${GATEWAY_XMS}" -Xmx"${GATEWAY_XMX}" -jar /opt/carbosys/GatewayServer.jar &

echo "Starting EgovHome..."
java -Xms"${EGOV_HOME_XMS}" -Xmx"${EGOV_HOME_XMX}" -jar /opt/carbosys/EgovHome.jar --server.port=18000 &

echo "Core services started (Gateway/EgovHome after Config readiness)."
echo "Other modules should be managed via MSA Manager UI."

# Start Business Services (Commented out to allow Manager control)
# echo "Starting business services..."
# java -jar /opt/carbosys/EgovMain.jar &
# java -jar /opt/carbosys/EgovLogin.jar &
# java -jar /opt/carbosys/EgovBoard.jar &
# java -jar /opt/carbosys/EgovJoin.jar &
# java -jar /opt/carbosys/EgovAuthor.jar &
# java -jar /opt/carbosys/EgovCmmnCode.jar &
# java -jar /opt/carbosys/EgovQuestionnaire.jar &
# java -jar /opt/carbosys/EgovSearch.jar &
# java -jar /opt/carbosys/EgovMobileId.jar &
# java -jar /opt/carbosys/EgovLoginPolicy.jar &

# Start UI Services (Commented out)
# echo "Starting UI services..."
# java -jar /opt/carbosys/home3.jar &
# java -jar /opt/carbosys/signin.jar &

echo "All services started!"
echo "Waiting for all processes..."

# Keep container running
tail -f /dev/null
