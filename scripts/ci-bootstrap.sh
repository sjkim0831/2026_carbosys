#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JENKINS_CONTAINER="${JENKINS_CONTAINER:-carbosys-jenkins}"
JOB_NAME="${JOB_NAME:-carbosys-main-cicd}"
REPO_URL="${REPO_URL:-https://github.com/sjkim0831/2026_carbosys.git}"
BRANCH_NAME="${BRANCH_NAME:-*/main}"
WEBHOOK_URL_OVERRIDE="${WEBHOOK_URL:-}"

cd "$ROOT_DIR"

echo "[1/6] Starting containers (build)..."
docker compose --profile ci up -d --build

echo "[2/6] Waiting for Jenkins..."
for i in $(seq 1 60); do
  if docker exec "$JENKINS_CONTAINER" sh -lc 'curl -fsS --max-time 3 http://localhost:8080/login >/dev/null'; then
    break
  fi
  sleep 3
  if [[ $i -eq 60 ]]; then
    echo "Jenkins not ready" >&2
    exit 1
  fi
done

ADMIN_PASS="$(docker exec "$JENKINS_CONTAINER" sh -lc 'cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || true')"
if [[ -z "$ADMIN_PASS" ]]; then
  echo "Jenkins initialAdminPassword not found. Complete setup once, then rerun." >&2
  exit 1
fi

echo "[3/6] Preparing Jenkins auth/session..."
docker exec "$JENKINS_CONTAINER" sh -lc "git config --global --add safe.directory /opt/carbosys || true"

COOKIE_FILE=/tmp/jenkins.cookie
CRUMB_JSON="$(docker exec "$JENKINS_CONTAINER" sh -lc "curl -sS -c $COOKIE_FILE -u admin:$ADMIN_PASS http://localhost:8080/crumbIssuer/api/json")"
CRUMB_FIELD="$(echo "$CRUMB_JSON" | jq -r '.crumbRequestField')"
CRUMB_VALUE="$(echo "$CRUMB_JSON" | jq -r '.crumb')"

echo "[4/6] Creating/updating Jenkins job..."
cat > /tmp/job-config.xml <<XML
<?xml version="1.1" encoding="UTF-8"?>
<flow-definition plugin="workflow-job">
  <actions/>
  <description>Carbosys main branch CI/CD pipeline</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>${REPO_URL}</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>${BRANCH_NAME}</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers>
    <com.cloudbees.jenkins.GitHubPushTrigger plugin="github"><spec/></com.cloudbees.jenkins.GitHubPushTrigger>
  </triggers>
  <disabled>false</disabled>
</flow-definition>
XML

docker cp /tmp/job-config.xml "$JENKINS_CONTAINER":/tmp/job-config.xml

JOB_CHECK_CODE="$(docker exec "$JENKINS_CONTAINER" sh -lc "curl -s -o /dev/null -w '%{http_code}' -u admin:$ADMIN_PASS http://localhost:8080/job/$JOB_NAME/api/json")"
if [[ "$JOB_CHECK_CODE" == "200" ]]; then
  docker exec "$JENKINS_CONTAINER" sh -lc "curl -sS -b $COOKIE_FILE -u admin:$ADMIN_PASS -H '$CRUMB_FIELD:$CRUMB_VALUE' -H 'Content-Type: application/xml' -X POST http://localhost:8080/job/$JOB_NAME/config.xml --data-binary @/tmp/job-config.xml >/dev/null"
else
  docker exec "$JENKINS_CONTAINER" sh -lc "curl -sS -b $COOKIE_FILE -u admin:$ADMIN_PASS -H '$CRUMB_FIELD:$CRUMB_VALUE' -H 'Content-Type: application/x-www-form-urlencoded' -X POST 'http://localhost:8080/createItem?name=$JOB_NAME' --data 'mode=org.jenkinsci.plugins.workflow.job.WorkflowJob&Submit=OK' >/dev/null"
  docker exec "$JENKINS_CONTAINER" sh -lc "curl -sS -b $COOKIE_FILE -u admin:$ADMIN_PASS -H '$CRUMB_FIELD:$CRUMB_VALUE' -H 'Content-Type: application/xml' -X POST http://localhost:8080/job/$JOB_NAME/config.xml --data-binary @/tmp/job-config.xml >/dev/null"
fi

echo "[5/6] Triggering first build..."
docker exec "$JENKINS_CONTAINER" sh -lc "curl -sS -b $COOKIE_FILE -u admin:$ADMIN_PASS -H '$CRUMB_FIELD:$CRUMB_VALUE' -X POST http://localhost:8080/job/$JOB_NAME/buildWithParameters --data '' >/dev/null"

echo "[6/6] Optional webhook setup..."
if [[ -n "${GITHUB_TOKEN:-}" ]]; then
  PUB_IP="$(curl -sS --max-time 8 https://api.ipify.org || true)"
  if [[ -n "$WEBHOOK_URL_OVERRIDE" ]]; then
    HOOK_URL="$WEBHOOK_URL_OVERRIDE"
  else
    HOOK_URL="http://${PUB_IP}:18081/github-webhook/"
  fi

  HOOK_PAYLOAD="$(jq -n --arg url "$HOOK_URL" '{name:"web",active:true,events:["push"],config:{url:$url,content_type:"json",insecure_ssl:"1"}}')"
  curl -sS --max-time 20 -X POST "https://api.github.com/repos/sjkim0831/2026_carbosys/hooks" \
    -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -d "$HOOK_PAYLOAD" >/tmp/github-hook.out || true
  echo "Webhook request sent: $HOOK_URL"
else
  echo "GITHUB_TOKEN not set. Skipping webhook creation."
fi

echo
echo "Done"
echo "Jenkins URL: http://localhost:18081"
echo "Job URL: http://localhost:18081/job/${JOB_NAME}/"
echo "Initial admin password: ${ADMIN_PASS}"
