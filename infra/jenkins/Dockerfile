FROM jenkins/jenkins:lts-jdk17

USER root
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    docker.io \
    git \
    jq \
    rsync \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
