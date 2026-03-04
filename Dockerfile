FROM node:20-bookworm-slim AS node_runtime

FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y \
    procps \
    net-tools \
    iproute2 \
    maven \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Bring Node.js runtime from official image without installing huge distro npm dependency tree.
COPY --from=node_runtime /usr/local/bin/node /usr/local/bin/node
COPY --from=node_runtime /usr/local/bin/npm /usr/local/bin/npm
COPY --from=node_runtime /usr/local/bin/npx /usr/local/bin/npx
COPY --from=node_runtime /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN ln -sf /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm && \
    ln -sf /usr/local/lib/node_modules/npm/bin/npx-cli.js /usr/local/bin/npx

WORKDIR /app

# Copy Config / Info files
COPY msa-mappings.yml /app/
COPY msa-ports.yml /app/

# Copy already built jars from host's target directories directly
COPY module/ConfigServer/target/ConfigServer.jar /app/
COPY module/EurekaServer/target/EurekaServer.jar /app/
COPY module/GatewayServer/target/GatewayServer.jar /app/
COPY module/EgovMsaManager/target/EgovMsaManager.jar /app/
COPY module/EgovLogin/target/EgovLogin.jar /app/
COPY module/EgovMain/target/EgovMain.jar /app/
COPY module/EgovBoard/target/EgovBoard.jar /app/
COPY module/EgovAuthor/target/EgovAuthor.jar /app/
COPY module/EgovCmmnCode/target/EgovCmmnCode.jar /app/
COPY module/EgovQuestionnaire/target/EgovQuestionnaire.jar /app/
COPY module/EgovSearch/target/EgovSearch.jar /app/
COPY module/EgovMobileId/target/EgovMobileId.jar /app/
COPY module/EgovLoginPolicy/target/EgovLoginPolicy.jar /app/
COPY module/EgovSR/target/EgovSR.jar /app/
COPY module/EgovCertLogin/target/EgovCertLogin.jar /app/
COPY module/EgovSimpleAuth/target/EgovSimpleAuth.jar /app/
COPY module/EgovGnrLogin/target/EgovGnrLogin.jar /app/
COPY module/EgovHome/target/EgovHome.jar /app/

# Create target directories so MsaScanner and other tools find files in their standard locations
RUN mkdir -p /app/EgovMain/target /app/EgovLogin/target /app/EgovBoard/target \
    /app/EgovAuthor/target /app/EgovCmmnCode/target /app/EgovQuestionnaire/target /app/EgovSearch/target \
    /app/EgovMobileId/target /app/EgovLoginPolicy/target /app/EgovSR/target /app/EgovCertLogin/target \
    /app/EgovSimpleAuth/target /app/EgovGnrLogin/target /app/EgovMsaManager/target \
    /app/EgovHome/target && \
    ln -sf /app/EgovMain.jar /app/EgovMain/target/EgovMain.jar && \
    ln -sf /app/EgovLogin.jar /app/EgovLogin/target/EgovLogin.jar && \
    ln -sf /app/EgovBoard.jar /app/EgovBoard/target/EgovBoard.jar && \
    ln -sf /app/EgovAuthor.jar /app/EgovAuthor/target/EgovAuthor.jar && \
    ln -sf /app/EgovCmmnCode.jar /app/EgovCmmnCode/target/EgovCmmnCode.jar && \
    ln -sf /app/EgovQuestionnaire.jar /app/EgovQuestionnaire/target/EgovQuestionnaire.jar && \
    ln -sf /app/EgovSearch.jar /app/EgovSearch/target/EgovSearch.jar && \
    ln -sf /app/EgovMobileId.jar /app/EgovMobileId/target/EgovMobileId.jar && \
    ln -sf /app/EgovLoginPolicy.jar /app/EgovLoginPolicy/target/EgovLoginPolicy.jar && \
    ln -sf /app/EgovSR.jar /app/EgovSR/target/EgovSR.jar && \
    ln -sf /app/EgovCertLogin.jar /app/EgovCertLogin/target/EgovCertLogin.jar && \
    ln -sf /app/EgovSimpleAuth.jar /app/EgovSimpleAuth/target/EgovSimpleAuth.jar && \
    ln -sf /app/EgovGnrLogin.jar /app/EgovGnrLogin/target/EgovGnrLogin.jar && \
    ln -sf /app/EgovMsaManager.jar /app/EgovMsaManager/target/EgovMsaManager.jar && \
    ln -sf /app/EgovHome.jar /app/EgovHome/target/EgovHome.jar

# Copy entrypoint script
COPY entrypoint.sh /app/
RUN chmod +x /app/entrypoint.sh

EXPOSE 8761 9000 18000 18011 18030

ENTRYPOINT /bin/sh /app/entrypoint.sh
