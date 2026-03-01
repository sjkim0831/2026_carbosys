# Jenkins CI/CD (운영/로컬 공용)

## 목표
- `main` 푸시 시 자동으로 변경 모듈만 빌드/배포
- 필요하면 로컬 프로젝트 폴더 변경 기반(working-tree)으로도 빌드/배포
- 배포는 MSA Manager API 사용(기본: 무중단)

## 1) Jenkins 컨테이너 실행
Linux/macOS:
```bash
./scripts/compose-up.sh ci --build
```

Windows PowerShell:
```powershell
./scripts/compose-up.ps1 -Profile ci -Build
```

- Jenkins URL: `http://localhost:18081`
- 초기 비밀번호:
```bash
docker exec carbosys-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## 2) Jenkins Job 설정
1. Pipeline Job 생성
2. Definition: `Pipeline script from SCM`
3. SCM: Git 저장소 + `main`
4. Script Path: `Jenkinsfile`
5. Trigger: GitHub webhook 또는 Poll SCM

## 3) Pipeline 파라미터
- `CHANGE_SOURCE`
- `git`: 커밋 간 변경 모듈 감지 (운영 기본)
- `working-tree`: 현재 폴더 변경분 기준 모듈 감지 (로컬 개발용)
- `BASE_COMMIT`, `HEAD_COMMIT`: `CHANGE_SOURCE=git`에서만 사용
- `DEPLOY_ROOT`: 기본 `/opt/carbosys`
- `APP_CONTAINER`: 기본 `carbosys-app`
- `MSA_MANAGER_URL`: 비우면 자동 감지(`carbosys-app` -> `localhost` 순서)

## 4) IP 비의존 동작 원칙
- 컨테이너 내부 통신은 서비스명(`carbosys-app`) 사용으로 IP와 무관
- 외부 webhook 진입 주소는 고정 도메인/터널 사용 권장
- 예: `https://jenkins.example.com/github-webhook/` 또는 고정 tunnel URL

## 5) 로컬 폴더 변경 기반 자동 배포
로컬 개발 PC에서 Git push 없이도 변경 감지 후 자동 빌드/배포 가능

Linux/macOS:
```bash
WATCH_INTERVAL_SEC=5 APP_CONTAINER=carbosys-app \
MSA_MANAGER_URL=http://localhost:18030/admin/msa \
./scripts/ci/watch_local_changes.sh
```

Windows PowerShell:
```powershell
./scripts/ci/watch_local_changes.ps1 -IntervalSec 5 -AppContainer carbosys-app -MsaManagerUrl http://localhost:18030/admin/msa
```

- 변경 감지 시 `working-tree` 모드로 해당 모듈만 빌드/배포
- `target`, `logs`, `data`, `file` 등은 감시 제외

## 6) 사용자 설정 필요 항목
- Jenkins Job에 Git 접근 자격증명 등록
- GitHub webhook URL 설정(운영 접근 가능한 고정 URL)
- 방화벽/포트: `18081`(Jenkins), 필요 시 reverse proxy 443
- 운영/개발 분리 시 개발용 Job은 비활성화

## 7) 배포 엔드포인트 정책
- 일반 모듈: `deploy-zerodowntime`
- `EgovMsaManager`: `deploy-restart` (자기 자신 무중단 배포 충돌 방지)
- 인프라 모듈(`EurekaServer`, `ConfigServer`, `GatewayServer`)은 자동 배포 대상 제외
