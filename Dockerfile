FROM node:20-bookworm-slim AS node_runtime

FROM eclipse-temurin:17-jdk
ARG APP_HOME=/opt/app
ENV APP_HOME=${APP_HOME}

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

WORKDIR ${APP_HOME}

# Copy Config / Info files
COPY msa-mappings.yml ${APP_HOME}/
COPY msa-ports.yml ${APP_HOME}/

# Copy already built jars from host's target directories directly
COPY module/ConfigServer/target/ConfigServer.jar ${APP_HOME}/
COPY module/EurekaServer/target/EurekaServer.jar ${APP_HOME}/
COPY module/GatewayServer/target/GatewayServer.jar ${APP_HOME}/
COPY module/EgovLogin/target/EgovLogin.jar ${APP_HOME}/
COPY module/EgovMain/target/EgovMain.jar ${APP_HOME}/
COPY module/EgovBoard/target/EgovBoard.jar ${APP_HOME}/
COPY module/EgovAuthor/target/EgovAuthor.jar ${APP_HOME}/
COPY module/EgovCmmnCode/target/EgovCmmnCode.jar ${APP_HOME}/
COPY module/EgovQuestionnaire/target/EgovQuestionnaire.jar ${APP_HOME}/
COPY module/EgovSearch/target/EgovSearch.jar ${APP_HOME}/
COPY module/EgovMobileId/target/EgovMobileId.jar ${APP_HOME}/
COPY module/EgovLoginPolicy/target/EgovLoginPolicy.jar ${APP_HOME}/
COPY module/EgovSR/target/EgovSR.jar ${APP_HOME}/
COPY module/EgovCertLogin/target/EgovCertLogin.jar ${APP_HOME}/
COPY module/EgovSimpleAuth/target/EgovSimpleAuth.jar ${APP_HOME}/
COPY module/EgovGnrLogin/target/EgovGnrLogin.jar ${APP_HOME}/
COPY module/EgovHome/target/EgovHome.jar ${APP_HOME}/

# Keep full module sources in image as a fallback build source when host bind mount is unavailable.
COPY module ${APP_HOME}/module

# Create target directories so MsaScanner and other tools find files in their standard locations
RUN mkdir -p ${APP_HOME}/EgovMain/target ${APP_HOME}/EgovLogin/target ${APP_HOME}/EgovBoard/target \
    ${APP_HOME}/EgovAuthor/target ${APP_HOME}/EgovCmmnCode/target ${APP_HOME}/EgovQuestionnaire/target ${APP_HOME}/EgovSearch/target \
    ${APP_HOME}/EgovMobileId/target ${APP_HOME}/EgovLoginPolicy/target ${APP_HOME}/EgovSR/target ${APP_HOME}/EgovCertLogin/target \
    ${APP_HOME}/EgovSimpleAuth/target ${APP_HOME}/EgovGnrLogin/target \
    ${APP_HOME}/EgovHome/target && \
    ln -sf ${APP_HOME}/EgovMain.jar ${APP_HOME}/EgovMain/target/EgovMain.jar && \
    ln -sf ${APP_HOME}/EgovLogin.jar ${APP_HOME}/EgovLogin/target/EgovLogin.jar && \
    ln -sf ${APP_HOME}/EgovBoard.jar ${APP_HOME}/EgovBoard/target/EgovBoard.jar && \
    ln -sf ${APP_HOME}/EgovAuthor.jar ${APP_HOME}/EgovAuthor/target/EgovAuthor.jar && \
    ln -sf ${APP_HOME}/EgovCmmnCode.jar ${APP_HOME}/EgovCmmnCode/target/EgovCmmnCode.jar && \
    ln -sf ${APP_HOME}/EgovQuestionnaire.jar ${APP_HOME}/EgovQuestionnaire/target/EgovQuestionnaire.jar && \
    ln -sf ${APP_HOME}/EgovSearch.jar ${APP_HOME}/EgovSearch/target/EgovSearch.jar && \
    ln -sf ${APP_HOME}/EgovMobileId.jar ${APP_HOME}/EgovMobileId/target/EgovMobileId.jar && \
    ln -sf ${APP_HOME}/EgovLoginPolicy.jar ${APP_HOME}/EgovLoginPolicy/target/EgovLoginPolicy.jar && \
    ln -sf ${APP_HOME}/EgovSR.jar ${APP_HOME}/EgovSR/target/EgovSR.jar && \
    ln -sf ${APP_HOME}/EgovCertLogin.jar ${APP_HOME}/EgovCertLogin/target/EgovCertLogin.jar && \
    ln -sf ${APP_HOME}/EgovSimpleAuth.jar ${APP_HOME}/EgovSimpleAuth/target/EgovSimpleAuth.jar && \
    ln -sf ${APP_HOME}/EgovGnrLogin.jar ${APP_HOME}/EgovGnrLogin/target/EgovGnrLogin.jar && \
    ln -sf ${APP_HOME}/EgovHome.jar ${APP_HOME}/EgovHome/target/EgovHome.jar

# Copy entrypoint script
COPY entrypoint.sh ${APP_HOME}/
RUN chmod +x ${APP_HOME}/entrypoint.sh

EXPOSE 8761 9000 18000

ENTRYPOINT /bin/sh ${APP_HOME}/entrypoint.sh
