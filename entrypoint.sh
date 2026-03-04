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

# Wait for CUBRID to be ready
echo "Waiting for CUBRID database (cas:33001)..."
MAX_RETRIES=30
COUNT=0
while ! (timeout 1 bash -c "</dev/tcp/cubrid/33001" 2>/dev/null); do
  COUNT=$((COUNT + 1))
  if [ $COUNT -ge $MAX_RETRIES ]; then
    echo "CUBRID is not ready after $MAX_RETRIES seconds. Proceeding anyway..."
    break
  fi
  sleep 2
done
echo "CUBRID CAS port is open. Waiting another 10s for initialization..."
sleep 10

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
