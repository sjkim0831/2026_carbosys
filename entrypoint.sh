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

# Start Business Services
echo "Starting business services..."

java -jar /app/EgovMain.jar &
java -jar /app/EgovLogin.jar &
java -jar /app/EgovBoard.jar &
java -jar /app/EgovJoin.jar &
java -jar /app/EgovAuthor.jar &
java -jar /app/EgovCmmnCode.jar &
java -jar /app/EgovQuestionnaire.jar &
java -jar /app/EgovSearch.jar &
java -jar /app/EgovMobileId.jar &
java -jar /app/EgovLoginPolicy.jar &

# Start Admin Services
echo "Starting admin services..."

java -jar /app/EgovAdminMain.jar &
java -jar /app/EgovAdminLogin.jar &

# Start Auth Services
echo "Starting auth services..."

java -jar /app/EgovCertLogin.jar &
java -jar /app/EgovSimpleAuth.jar &
java -jar /app/EgovGnrLogin.jar &

# Start UI Services
echo "Starting UI services..."

java -jar /app/home3.jar &
java -jar /app/signin.jar &

echo "All services started!"
echo "Waiting for all processes..."

# Keep container running
tail -f /dev/null
