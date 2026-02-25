# Runtime Stage (Lightweight JRE)
FROM eclipse-temurin:8-jre

# Install essential tools for monitoring inside container
RUN apt-get update && apt-get install -y procps net-tools iproute2 curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy ALL source code/config files needed by MsaManager for scanning
COPY . .

# Copy existing JAR files from the host to the container
COPY EurekaServer/target/EurekaServer.jar /app/EurekaServer.jar
COPY ConfigServer/target/ConfigServer.jar /app/ConfigServer.jar
COPY GatewayServer/target/GatewayServer.jar /app/GatewayServer.jar
COPY EgovMsaManager/target/EgovMsaManager.jar /app/EgovMsaManager.jar
COPY signin/target/signin.jar /app/signin/target/signin.jar

# Copy entrypoint script
RUN chmod +x /app/entrypoint.sh

# Expose required ports
EXPOSE 8761 9000 18030

ENTRYPOINT ["/app/entrypoint.sh"]
