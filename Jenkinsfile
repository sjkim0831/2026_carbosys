pipeline {
  agent any

  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  parameters {
    choice(name: 'CHANGE_SOURCE', choices: ['git', 'working-tree'], description: '모듈 변경 감지 기준')
    string(name: 'BASE_COMMIT', defaultValue: '', description: 'CHANGE_SOURCE=git 일 때 기준 커밋(비우면 자동)')
    string(name: 'HEAD_COMMIT', defaultValue: 'HEAD', description: 'CHANGE_SOURCE=git 일 때 대상 커밋')
    string(name: 'DEPLOY_ROOT', defaultValue: '/opt/carbosys', description: '배포 루트 경로')
    string(name: 'APP_CONTAINER', defaultValue: 'carbosys-app', description: '빌드 실행 대상 컨테이너')
    string(name: 'MSA_MANAGER_URL', defaultValue: 'http://carbosys-app:18030/admin/msa', description: 'MSA Manager API URL')
  }

  environment {
    CHANGE_SOURCE = "${params.CHANGE_SOURCE}"
    BASE_COMMIT = "${params.BASE_COMMIT}"
    HEAD_COMMIT = "${params.HEAD_COMMIT}"
    DEPLOY_ROOT = "${params.DEPLOY_ROOT}"
    APP_CONTAINER = "${params.APP_CONTAINER}"
    MSA_MANAGER_URL = "${params.MSA_MANAGER_URL}"
  }

  triggers {
    pollSCM('H/5 * * * *')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Sync To Deploy Root') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
rsync -a \
  --exclude='.git/' \
  --exclude='logs/' \
  --exclude='data/' \
  --exclude='file/' \
  --exclude='wsl.localhost/' \
  --exclude='module/*/target/' \
  --exclude='module/EgovMsaManager/runtime/' \
  "$WORKSPACE/" "$DEPLOY_ROOT/"
'''
      }
    }

    stage('Build + Deploy Changed Modules') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
cd "$DEPLOY_ROOT"
ARGS=(--source "$CHANGE_SOURCE")
if [[ "$CHANGE_SOURCE" == "git" ]]; then
  [[ -n "${BASE_COMMIT:-}" ]] && ARGS+=(--base "$BASE_COMMIT")
  [[ -n "${HEAD_COMMIT:-}" ]] && ARGS+=(--head "$HEAD_COMMIT")
fi
APP_CONTAINER="$APP_CONTAINER" MSA_MANAGER_URL="$MSA_MANAGER_URL" \
  scripts/ci/run_changed_modules_pipeline.sh "${ARGS[@]}"
'''
      }
    }

    stage('Verify') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
curl -fsS "$MSA_MANAGER_URL/api/modules" | jq -r '.[] | [.id, .status, .port] | @tsv'
'''
      }
    }
  }

  post {
    success {
      echo 'Jenkins CI/CD completed successfully.'
    }
    failure {
      echo 'Jenkins CI/CD failed. Check build logs and MSA manager status.'
    }
  }
}
