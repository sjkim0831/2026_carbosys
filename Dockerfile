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

WORKDIR /opt/carbosys

# Copy Config / Info files
COPY msa-mappings.yml /opt/carbosys/
COPY msa-ports.yml /opt/carbosys/

# Copy already built jars from host's target directories directly
COPY module/ConfigServer/target/ConfigServer.jar /opt/carbosys/
COPY module/EurekaServer/target/EurekaServer.jar /opt/carbosys/
COPY module/GatewayServer/target/GatewayServer.jar /opt/carbosys/
COPY module/EgovLogin/target/EgovLogin.jar /opt/carbosys/
COPY module/EgovMain/target/EgovMain.jar /opt/carbosys/
COPY module/EgovBoard/target/EgovBoard.jar /opt/carbosys/
COPY module/EgovAuthor/target/EgovAuthor.jar /opt/carbosys/
COPY module/EgovCmmnCode/target/EgovCmmnCode.jar /opt/carbosys/
COPY module/EgovQuestionnaire/target/EgovQuestionnaire.jar /opt/carbosys/
COPY module/EgovSearch/target/EgovSearch.jar /opt/carbosys/
COPY module/EgovMobileId/target/EgovMobileId.jar /opt/carbosys/
COPY module/EgovLoginPolicy/target/EgovLoginPolicy.jar /opt/carbosys/
COPY module/EgovSR/target/EgovSR.jar /opt/carbosys/
COPY module/EgovCertLogin/target/EgovCertLogin.jar /opt/carbosys/
COPY module/EgovSimpleAuth/target/EgovSimpleAuth.jar /opt/carbosys/
COPY module/EgovGnrLogin/target/EgovGnrLogin.jar /opt/carbosys/
COPY module/EgovHome/target/EgovHome.jar /opt/carbosys/

# Keep full module sources in image as a fallback build source when host bind mount is unavailable.
COPY module /opt/carbosys/module

# Create target directories so MsaScanner and other tools find files in their standard locations
RUN mkdir -p /opt/carbosys/EgovMain/target /opt/carbosys/EgovLogin/target /opt/carbosys/EgovBoard/target \
    /opt/carbosys/EgovAuthor/target /opt/carbosys/EgovCmmnCode/target /opt/carbosys/EgovQuestionnaire/target /opt/carbosys/EgovSearch/target \
    /opt/carbosys/EgovMobileId/target /opt/carbosys/EgovLoginPolicy/target /opt/carbosys/EgovSR/target /opt/carbosys/EgovCertLogin/target \
    /opt/carbosys/EgovSimpleAuth/target /opt/carbosys/EgovGnrLogin/target \
    /opt/carbosys/EgovHome/target && \
    ln -sf /opt/carbosys/EgovMain.jar /opt/carbosys/EgovMain/target/EgovMain.jar && \
    ln -sf /opt/carbosys/EgovLogin.jar /opt/carbosys/EgovLogin/target/EgovLogin.jar && \
    ln -sf /opt/carbosys/EgovBoard.jar /opt/carbosys/EgovBoard/target/EgovBoard.jar && \
    ln -sf /opt/carbosys/EgovAuthor.jar /opt/carbosys/EgovAuthor/target/EgovAuthor.jar && \
    ln -sf /opt/carbosys/EgovCmmnCode.jar /opt/carbosys/EgovCmmnCode/target/EgovCmmnCode.jar && \
    ln -sf /opt/carbosys/EgovQuestionnaire.jar /opt/carbosys/EgovQuestionnaire/target/EgovQuestionnaire.jar && \
    ln -sf /opt/carbosys/EgovSearch.jar /opt/carbosys/EgovSearch/target/EgovSearch.jar && \
    ln -sf /opt/carbosys/EgovMobileId.jar /opt/carbosys/EgovMobileId/target/EgovMobileId.jar && \
    ln -sf /opt/carbosys/EgovLoginPolicy.jar /opt/carbosys/EgovLoginPolicy/target/EgovLoginPolicy.jar && \
    ln -sf /opt/carbosys/EgovSR.jar /opt/carbosys/EgovSR/target/EgovSR.jar && \
    ln -sf /opt/carbosys/EgovCertLogin.jar /opt/carbosys/EgovCertLogin/target/EgovCertLogin.jar && \
    ln -sf /opt/carbosys/EgovSimpleAuth.jar /opt/carbosys/EgovSimpleAuth/target/EgovSimpleAuth.jar && \
    ln -sf /opt/carbosys/EgovGnrLogin.jar /opt/carbosys/EgovGnrLogin/target/EgovGnrLogin.jar && \
    ln -sf /opt/carbosys/EgovHome.jar /opt/carbosys/EgovHome/target/EgovHome.jar

# Copy entrypoint script
COPY entrypoint.sh /opt/carbosys/
RUN chmod +x /opt/carbosys/entrypoint.sh

EXPOSE 8761 9000 18000

ENTRYPOINT /bin/sh /opt/carbosys/entrypoint.sh
