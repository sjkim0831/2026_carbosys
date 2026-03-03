pipeline {
  agent any

  options {
    skipDefaultCheckout(true)
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  parameters {
    choice(name: 'DEPLOY_TARGET', choices: ['dev-local', 'prod-remote'], description: '배포 대상 모드')
    string(name: 'REPO_URL', defaultValue: 'https://github.com/sjkim0831/2026_carbosys.git', description: 'Git 저장소 URL')
    string(name: 'DEV_BRANCH', defaultValue: 'main', description: '개발 서버 pull 대상 브랜치')
    string(name: 'RELEASE_BRANCH', defaultValue: 'release', description: '운영 배포 대상 브랜치')
    choice(name: 'CHANGE_SOURCE', choices: ['git', 'working-tree'], description: '모듈 변경 감지 기준')
    string(name: 'BASE_COMMIT', defaultValue: '', description: 'CHANGE_SOURCE=git 일 때 기준 커밋(비우면 자동)')
    string(name: 'HEAD_COMMIT', defaultValue: 'HEAD', description: 'CHANGE_SOURCE=git 일 때 대상 커밋')
    string(name: 'DEPLOY_ROOT', defaultValue: '/opt/carbosys', description: '개발 서버 배포 루트')
    string(name: 'APP_CONTAINER', defaultValue: 'carbosys-app', description: '빌드 실행 대상 컨테이너')
    string(name: 'MSA_MANAGER_URL', defaultValue: 'http://carbosys-app:18030/admin/msa', description: '개발 서버 MSA Manager API URL')
    string(name: 'PROD_SSH_HOST', defaultValue: '', description: '운영 서버 SSH 호스트/IP')
    string(name: 'PROD_SSH_PORT', defaultValue: '22', description: '운영 서버 SSH 포트')
    string(name: 'PROD_SSH_USER', defaultValue: '', description: '운영 서버 SSH 사용자')
    string(name: 'PROD_SSH_KEY_PATH', defaultValue: '/var/jenkins_home/.ssh/id_rsa', description: '운영 서버 SSH 키 경로(Jenkins 컨테이너 내부)')
    string(name: 'PROD_DEPLOY_ROOT', defaultValue: '/opt/carbosys', description: '운영 서버 배포 루트')
    string(name: 'PROD_MSA_MANAGER_URL', defaultValue: '', description: '운영 서버 MSA Manager API URL')
    choice(name: 'PROD_DEPLOY_ENGINE', choices: ['msa-api', 'direct-restart'], description: '운영 배포 엔진')
    string(name: 'PROD_APP_CONTAINER', defaultValue: 'carbosys-app', description: '운영 앱 컨테이너명')
  }

  environment {
    DEPLOY_TARGET = "${params.DEPLOY_TARGET}"
    REPO_URL = "${params.REPO_URL}"
    DEV_BRANCH = "${params.DEV_BRANCH}"
    RELEASE_BRANCH = "${params.RELEASE_BRANCH}"
    CHANGE_SOURCE = "${params.CHANGE_SOURCE}"
    BASE_COMMIT = "${params.BASE_COMMIT}"
    HEAD_COMMIT = "${params.HEAD_COMMIT}"
    DEPLOY_ROOT = "${params.DEPLOY_ROOT}"
    APP_CONTAINER = "${params.APP_CONTAINER}"
    MSA_MANAGER_URL = "${params.MSA_MANAGER_URL}"
    PROD_SSH_HOST = "${params.PROD_SSH_HOST}"
    PROD_SSH_PORT = "${params.PROD_SSH_PORT}"
    PROD_SSH_USER = "${params.PROD_SSH_USER}"
    PROD_SSH_KEY_PATH = "${params.PROD_SSH_KEY_PATH}"
    PROD_DEPLOY_ROOT = "${params.PROD_DEPLOY_ROOT}"
    PROD_MSA_MANAGER_URL = "${params.PROD_MSA_MANAGER_URL}"
    PROD_DEPLOY_ENGINE = "${params.PROD_DEPLOY_ENGINE}"
    PROD_APP_CONTAINER = "${params.PROD_APP_CONTAINER}"
  }

  triggers {
    pollSCM('H/5 * * * *')
  }

  stages {
    stage('Git Pull In Deploy Root (dev-local)') {
      when { expression { params.DEPLOY_TARGET == 'dev-local' } }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
git config --global --add safe.directory "$DEPLOY_ROOT" || true
cd "$DEPLOY_ROOT"
if [[ ! -d .git ]]; then
  echo "DEPLOY_ROOT is not a git repository: $DEPLOY_ROOT" >&2
  exit 1
fi

BASE_COMMIT_LOCAL="$(git rev-parse HEAD)"
git fetch --all --prune
git checkout "$DEV_BRANCH"
git reset --hard "origin/$DEV_BRANCH"
HEAD_COMMIT_LOCAL="$(git rev-parse HEAD)"

echo "$BASE_COMMIT_LOCAL" > "$DEPLOY_ROOT/.ci_base_commit"
echo "$HEAD_COMMIT_LOCAL" > "$DEPLOY_ROOT/.ci_head_commit"
echo "Pulled in $DEPLOY_ROOT: $BASE_COMMIT_LOCAL -> $HEAD_COMMIT_LOCAL"
'''
      }
    }

    stage('Build + Deploy Changed Modules (dev-local)') {
      when { expression { params.DEPLOY_TARGET == 'dev-local' } }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
cd "$DEPLOY_ROOT"
BASE_COMMIT_LOCAL="$(cat "$DEPLOY_ROOT/.ci_base_commit" 2>/dev/null || true)"
HEAD_COMMIT_LOCAL="$(cat "$DEPLOY_ROOT/.ci_head_commit" 2>/dev/null || true)"
if [[ -z "$HEAD_COMMIT_LOCAL" ]]; then
  HEAD_COMMIT_LOCAL="$(git rev-parse HEAD)"
fi

ARGS=(--source "$CHANGE_SOURCE")
if [[ "$CHANGE_SOURCE" == "git" ]]; then
  if [[ -n "$BASE_COMMIT_LOCAL" ]]; then
    ARGS+=(--base "$BASE_COMMIT_LOCAL")
  elif [[ -n "${BASE_COMMIT:-}" ]]; then
    ARGS+=(--base "$BASE_COMMIT")
  fi
  ARGS+=(--head "$HEAD_COMMIT_LOCAL")
fi
APP_CONTAINER="$APP_CONTAINER" MSA_MANAGER_URL="$MSA_MANAGER_URL" \
  bash scripts/ci/run_changed_modules_pipeline.sh "${ARGS[@]}"
'''
      }
    }

    stage('Checkout Release In Jenkins Workspace (prod-remote)') {
      when { expression { params.DEPLOY_TARGET == 'prod-remote' } }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
git init .
if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$REPO_URL"
else
  git remote add origin "$REPO_URL"
fi
git fetch --prune origin "$RELEASE_BRANCH"
git checkout -B "$RELEASE_BRANCH" "origin/$RELEASE_BRANCH"
git reset --hard "origin/$RELEASE_BRANCH"
git clean -fdx
'''
      }
    }

    stage('Build + Deploy Changed Modules (prod-remote)') {
      when { expression { params.DEPLOY_TARGET == 'prod-remote' } }
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail

if [[ -z "$PROD_SSH_HOST" || -z "$PROD_SSH_USER" ]]; then
  echo "PROD_SSH_HOST and PROD_SSH_USER are required for prod-remote mode." >&2
  exit 1
fi
if [[ ! -f "$PROD_SSH_KEY_PATH" ]]; then
  echo "SSH key not found: $PROD_SSH_KEY_PATH" >&2
  exit 1
fi

BASE_COMMIT_LOCAL="${GIT_PREVIOUS_SUCCESSFUL_COMMIT:-}"
HEAD_COMMIT_LOCAL="${GIT_COMMIT:-HEAD}"

ARGS=(--source git)
[[ -n "$BASE_COMMIT_LOCAL" ]] && ARGS+=(--base "$BASE_COMMIT_LOCAL")
ARGS+=(--head "$HEAD_COMMIT_LOCAL")

mapfile -t MODS < <(bash scripts/ci/detect_changed_modules.sh "${ARGS[@]}" || true)
if [[ ${#MODS[@]} -eq 0 ]]; then
  echo "No changed modules detected for prod-remote"
  exit 0
fi

echo "Detected modules: ${MODS[*]}"
bash scripts/ci/build_modules_in_container.sh "${MODS[@]}"

PROD_SSH_HOST="$PROD_SSH_HOST" \
PROD_SSH_PORT="$PROD_SSH_PORT" \
PROD_SSH_USER="$PROD_SSH_USER" \
PROD_SSH_KEY_PATH="$PROD_SSH_KEY_PATH" \
PROD_DEPLOY_ROOT="$PROD_DEPLOY_ROOT" \
PROD_MSA_MANAGER_URL="$PROD_MSA_MANAGER_URL" \
PROD_DEPLOY_ENGINE="$PROD_DEPLOY_ENGINE" \
PROD_APP_CONTAINER="$PROD_APP_CONTAINER" \
bash scripts/ci/deploy_prod_remote.sh "${MODS[@]}"
'''
      }
    }

    stage('Verify') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
if [[ "$DEPLOY_TARGET" == "prod-remote" ]]; then
  if [[ "$PROD_DEPLOY_ENGINE" == "msa-api" ]]; then
    echo "[VERIFY][PROD][MSA-API]"
    curl -fsS "$PROD_MSA_MANAGER_URL/api/modules" | jq -r '.[] | [.id, .status, .port] | @tsv'
  else
    echo "[VERIFY][PROD][DIRECT-RESTART] MSA API 검증 생략"
  fi
else
  echo "[VERIFY][DEV]"
  curl -fsS "$MSA_MANAGER_URL/api/modules" | jq -r '.[] | [.id, .status, .port] | @tsv'
fi
'''
      }
    }
  }

  post {
    always {
      script {
        if (params.DEPLOY_TARGET == 'prod-remote') {
          deleteDir()
          echo 'Workspace cleaned (prod-remote source removed).'
        }
      }
    }
    success {
      echo 'Jenkins CI/CD completed successfully.'
    }
    failure {
      echo 'Jenkins CI/CD failed. Check build logs and MSA manager status.'
    }
  }
}
