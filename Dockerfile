# Build Stage
FROM eclipse-temurin:8-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /build

# Copy all module sources
COPY module/ConfigServer /build/ConfigServer
COPY module/EurekaServer /build/EurekaServer
COPY module/GatewayServer /build/GatewayServer
COPY module/EgovMsaManager /build/EgovMsaManager
COPY module/EgovJoin /build/EgovJoin
COPY module/EgovLogin /build/EgovLogin
COPY module/EgovMain /build/EgovMain
COPY module/EgovBoard /build/EgovBoard
COPY module/EgovAuthor /build/EgovAuthor
COPY module/EgovCmmnCode /build/EgovCmmnCode
COPY module/EgovQuestionnaire /build/EgovQuestionnaire
COPY module/EgovSearch /build/EgovSearch
COPY module/EgovMobileId /build/EgovMobileId
COPY module/EgovLoginPolicy /build/EgovLoginPolicy
COPY module/EgovAdminLogin /build/EgovAdminLogin
COPY module/EgovAdminMain /build/EgovAdminMain
COPY module/EgovCertLogin /build/EgovCertLogin
COPY module/EgovSimpleAuth /build/EgovSimpleAuth
COPY module/EgovGnrLogin /build/EgovGnrLogin
COPY module/home3 /build/home3
COPY module/signin /build/signin

# Build all modules
RUN cd /build/ConfigServer && mvn clean package -DskipTests -q && cp target/ConfigServer.jar /output/
RUN cd /build/EurekaServer && mvn clean package -DskipTests -q && cp target/EurekaServer.jar /output/
RUN cd /build/GatewayServer && mvn clean package -DskipTests -q && cp target/GatewayServer.jar /output/
RUN cd /build/EgovMsaManager && mvn clean package -DskipTests -q && cp target/EgovMsaManager.jar /output/
RUN cd /build/EgovJoin && mvn clean package -DskipTests -q && cp target/EgovJoin.jar /output/
RUN cd /build/EgovLogin && mvn clean package -DskipTests -q && cp target/EgovLogin.jar /output/
RUN cd /build/EgovMain && mvn clean package -DskipTests -q && cp target/EgovMain.jar /output/
RUN cd /build/EgovBoard && mvn clean package -DskipTests -q && cp target/EgovBoard.jar /output/
RUN cd /build/EgovAuthor && mvn clean package -DskipTests -q && cp target/EgovAuthor.jar /output/
RUN cd /build/EgovCmmnCode && mvn clean package -DskipTests -q && cp target/EgovCmmnCode.jar /output/
RUN cd /build/EgovQuestionnaire && mvn clean package -DskipTests -q && cp target/EgovQuestionnaire.jar /output/
RUN cd /build/EgovSearch && mvn clean package -DskipTests -q && cp target/EgovSearch.jar /output/
RUN cd /build/EgovMobileId && mvn clean package -DskipTests -q && cp target/EgovMobileId.jar /output/
RUN cd /build/EgovLoginPolicy && mvn clean package -DskipTests -q && cp target/EgovLoginPolicy.jar /output/
RUN cd /build/EgovAdminLogin && mvn clean package -DskipTests -q && cp target/EgovAdminLogin.jar /output/
RUN cd /build/EgovAdminMain && mvn clean package -DskipTests -q && cp target/EgovAdminMain.jar /output/
RUN cd /build/EgovCertLogin && mvn clean package -DskipTests -q && cp target/EgovCertLogin.jar /output/
RUN cd /build/EgovSimpleAuth && mvn clean package -DskipTests -q && cp target/EgovSimpleAuth.jar /output/
RUN cd /build/EgovGnrLogin && mvn clean package -DskipTests -q && cp target/EgovGnrLogin.jar /output/
RUN cd /build/home3 && mvn clean package -DskipTests -q && cp target/home3.jar /output/
RUN cd /build/signin && mvn clean package -DskipTests -q && cp target/signin.jar /output/

# Copy MSA config files
COPY msa-mappings.yml /output/
COPY msa-ports.yml /output/

# Runtime Stage
FROM eclipse-temurin:8-jre

RUN apt-get update && apt-get install -y procps net-tools iproute2 curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy all JARs from builder
COPY --from=builder /output/*.jar /app/

# Create module directories and symlinks
RUN mkdir -p /app/EgovMain/target /app/EgovLogin/target /app/EgovBoard/target /app/EgovJoin/target \
    /app/EgovAuthor/target /app/EgovCmmnCode/target /app/EgovQuestionnaire/target /app/EgovSearch/target \
    /app/EgovMobileId/target /app/EgovLoginPolicy/target /app/home3/target /app/signin/target /app/EgovAdminMain/target \
    /app/EgovAdminLogin/target /app/EgovCertLogin/target /app/EgovSimpleAuth/target /app/EgovGnrLogin/target && \
    ln -sf /app/EgovMain.jar /app/EgovMain/target/EgovMain.jar && \
    ln -sf /app/EgovLogin.jar /app/EgovLogin/target/EgovLogin.jar && \
    ln -sf /app/EgovBoard.jar /app/EgovBoard/target/EgovBoard.jar && \
    ln -sf /app/EgovJoin.jar /app/EgovJoin/target/EgovJoin.jar && \
    ln -sf /app/EgovAuthor.jar /app/EgovAuthor/target/EgovAuthor.jar && \
    ln -sf /app/EgovCmmnCode.jar /app/EgovCmmnCode/target/EgovCmmnCode.jar && \
    ln -sf /app/EgovQuestionnaire.jar /app/EgovQuestionnaire/target/EgovQuestionnaire.jar && \
    ln -sf /app/EgovSearch.jar /app/EgovSearch/target/EgovSearch.jar && \
    ln -sf /app/EgovMobileId.jar /app/EgovMobileId/target/EgovMobileId.jar && \
    ln -sf /app/EgovLoginPolicy.jar /app/EgovLoginPolicy/target/EgovLoginPolicy.jar && \
    ln -sf /app/home3.jar /app/home3/target/home3.jar && \
    ln -sf /app/signin.jar /app/signin/target/signin.jar && \
    ln -sf /app/EgovAdminMain.jar /app/EgovAdminMain/target/EgovAdminMain.jar && \
    ln -sf /app/EgovAdminLogin.jar /app/EgovAdminLogin/target/EgovAdminLogin.jar && \
    ln -sf /app/EgovCertLogin.jar /app/EgovCertLogin/target/EgovCertLogin.jar && \
    ln -sf /app/EgovSimpleAuth.jar /app/EgovSimpleAuth/target/EgovSimpleAuth.jar && \
    ln -sf /app/EgovGnrLogin.jar /app/EgovGnrLogin/target/EgovGnrLogin.jar

# Copy entrypoint script
COPY entrypoint.sh /app/
RUN chmod +x /app/entrypoint.sh

EXPOSE 8761 9000 18030

ENTRYPOINT ["/app/entrypoint.sh"]
