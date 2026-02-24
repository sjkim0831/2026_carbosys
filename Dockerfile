FROM maven:3.8.4-openjdk-8

# Install essential tools
RUN apt-get update && apt-get install -y procps net-tools iproute2 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the entire workspace
COPY . .

# Copy and install local dependencies to local maven repo inside Docker
COPY libs/cubrid-jdbc-11.2.0.0035.jar /app/libs/cubrid-jdbc-11.2.0.0035.jar
RUN mvn install:install-file -Dfile=/app/libs/cubrid-jdbc-11.2.0.0035.jar -DgroupId=cubrid -DartifactId=cubrid-jdbc -Dversion=11.2.0.0035 -Dpackaging=jar

# Build infrastructure modules one by one to identify failures
RUN mvn install -DskipTests -pl EurekaServer -am
RUN mvn install -DskipTests -pl ConfigServer -am
RUN mvn install -DskipTests -pl GatewayServer -am
RUN mvn install -DskipTests -pl EgovMsaManager -am

# Expose only the required ports
EXPOSE 8761 9000 18030

# Ensure entrypoint is executable
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
