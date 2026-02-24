#!/bin/bash

# Navigate to the app directory
cd /app

echo "===================================================="
echo "Starting Carbosys Core Infrastructure..."
echo "===================================================="

# 1. Start Eureka Server
echo "[1/4] Starting Eureka Server (8761)..."
nohup mvn -pl EurekaServer spring-boot:run > /app/eureka.log 2>&1 &

# Wait for Eureka to stabilize
sleep 15

# 2. Start Config Server
echo "[2/4] Starting Config Server (8888)..."
nohup mvn -pl ConfigServer spring-boot:run > /app/config.log 2>&1 &

# Wait for Config Server to fetch settings
sleep 20

# 3. Start Gateway Server
echo "[3/4] Starting Gateway Server (9000)..."
nohup mvn -pl GatewayServer spring-boot:run > /app/gateway.log 2>&1 &

# Wait for Gateway
sleep 10

# 4. Start MSA Manager (Main Process)
echo "[4/4] Starting MSA Manager (18030)..."
echo "All infrastructure services are running in background."
echo "You can now manage other modules via http://localhost:18030/admin/msa/"
echo "===================================================="

# Run MsaManager in foreground to keep container alive
mvn -pl EgovMsaManager spring-boot:run
