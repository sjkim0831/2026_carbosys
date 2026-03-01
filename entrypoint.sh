#!/bin/bash

set -e

echo "Starting Carbosys MSA Services..."

# Wait for CUBRID to be ready
echo "Waiting for CUBRID database..."
sleep 10

# Start Infrastructure Services (Eureka, Config, Gateway)
echo "Starting EurekaServer..."
java -jar /app/EurekaServer.jar &

echo "Starting ConfigServer..."
java -jar /app/ConfigServer.jar &

# Wait for Eureka and Config to be ready
echo "Waiting for infrastructure services..."
sleep 30

echo "Starting GatewayServer..."
java -jar /app/GatewayServer.jar &

# Start MSA Manager
echo "Starting EgovMsaManager..."
java -jar /app/EgovMsaManager.jar &

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
java -jar /app/EgovHome.jar --server.port=18000 &

echo "All services started!"
echo "Waiting for all processes..."

# Keep container running
tail -f /dev/null
