#!/bin/bash

set -e

echo "Starting Carbosys MSA Services..."

# JVM memory defaults (override via env if needed)
EUREKA_XMS="${EUREKA_XMS:-128m}"
EUREKA_XMX="${EUREKA_XMX:-256m}"
CONFIG_XMS="${CONFIG_XMS:-128m}"
CONFIG_XMX="${CONFIG_XMX:-256m}"
GATEWAY_XMS="${GATEWAY_XMS:-128m}"
GATEWAY_XMX="${GATEWAY_XMX:-384m}"
MSA_MANAGER_XMS="${MSA_MANAGER_XMS:-256m}"
MSA_MANAGER_XMX="${MSA_MANAGER_XMX:-512m}"
EGOV_HOME_XMS="${EGOV_HOME_XMS:-128m}"
EGOV_HOME_XMX="${EGOV_HOME_XMX:-384m}"

# Wait for CUBRID to be really ready (both broker and CAS, consecutive checks)
echo "Waiting for CUBRID broker (33000) and CAS (33001)..."
MAX_RETRIES="${CUBRID_MAX_RETRIES:-120}"
SLEEP_SEC="${CUBRID_RETRY_INTERVAL_SEC:-2}"
REQUIRED_STABLE="${CUBRID_REQUIRED_STABLE:-5}"
REQUIRE_CAS="${CUBRID_REQUIRE_CAS:-false}"
COUNT=0
STABLE=0
while true; do
  BROKER_OK=0
  CAS_OK=0

  if timeout 1 bash -c "</dev/tcp/cubrid/33000" 2>/dev/null; then
    BROKER_OK=1
  fi
  if timeout 1 bash -c "</dev/tcp/cubrid/33001" 2>/dev/null; then
    CAS_OK=1
  fi

  READY=0
  if [ "$BROKER_OK" -eq 1 ]; then
    if [ "$REQUIRE_CAS" = "true" ]; then
      [ "$CAS_OK" -eq 1 ] && READY=1
    else
      READY=1
    fi
  fi

  if [ "$READY" -eq 1 ]; then
    STABLE=$((STABLE + 1))
    echo "CUBRID readiness check passed (${STABLE}/${REQUIRED_STABLE})"
    if [ "$STABLE" -ge "$REQUIRED_STABLE" ]; then
      break
    fi
  else
    if [ "$STABLE" -gt 0 ]; then
      echo "CUBRID readiness lost; resetting stability counter."
    fi
    STABLE=0
  fi

  COUNT=$((COUNT + 1))
  if [ "$COUNT" -ge "$MAX_RETRIES" ]; then
    echo "CUBRID is not stably ready after $((MAX_RETRIES * SLEEP_SEC)) seconds. Proceeding anyway..."
    break
  fi
  sleep "$SLEEP_SEC"
done
echo "CUBRID stable readiness wait completed."

# Start Infrastructure Services (Eureka, Config, Gateway)
echo "Starting EurekaServer..."
java -Xms"${EUREKA_XMS}" -Xmx"${EUREKA_XMX}" -jar /app/EurekaServer.jar &

echo "Starting ConfigServer..."
java -Xms"${CONFIG_XMS}" -Xmx"${CONFIG_XMX}" -jar /app/ConfigServer.jar &

# Wait for Eureka and Config to be ready
echo "Waiting for infrastructure services..."
sleep 30

echo "Starting GatewayServer..."
java -Xms"${GATEWAY_XMS}" -Xmx"${GATEWAY_XMX}" -jar /app/GatewayServer.jar &

# Start MSA Manager
echo "Starting EgovMsaManager..."
java -Xms"${MSA_MANAGER_XMS}" -Xmx"${MSA_MANAGER_XMX}" -jar /app/EgovMsaManager.jar &

echo "Core services (Eureka, Config, Gateway, Manager) started!"
echo "Other modules should be managed via MSA Manager UI."

# Start Business Services (Commented out to allow Manager control)
# echo "Starting business services..."
# java -jar /app/EgovMain.jar &
# java -jar /app/EgovLogin.jar &
# java -jar /app/EgovBoard.jar &
# java -jar /app/EgovJoin.jar &
# java -jar /app/EgovAuthor.jar &
# java -jar /app/EgovCmmnCode.jar &
# java -jar /app/EgovQuestionnaire.jar &
# java -jar /app/EgovSearch.jar &
# java -jar /app/EgovMobileId.jar &
# java -jar /app/EgovLoginPolicy.jar &

# Start UI Services (Commented out)
# echo "Starting UI services..."
# java -jar /app/home3.jar &
# java -jar /app/signin.jar &

# Start merged EgovHome service (EgovJoin + home3 + signin)
echo "Starting EgovHome..."
java -Xms"${EGOV_HOME_XMS}" -Xmx"${EGOV_HOME_XMX}" -jar /app/EgovHome.jar --server.port=18000 &

echo "All services started!"
echo "Waiting for all processes..."

# Keep container running
tail -f /dev/null
