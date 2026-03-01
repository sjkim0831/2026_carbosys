# CI/CD 빠른 실행

## 1) Jenkins 자동 구성 + 첫 빌드
Linux/macOS:
```bash
./scripts/jenkins-up.sh
```

Windows PowerShell:
```powershell
./scripts/jenkins-up.ps1
```

위 명령 1개로 아래를 자동 수행합니다.
- Jenkins/앱 컨테이너 빌드 및 기동
- Jenkins 준비 대기
- `carbosys-main-cicd` Job 자동 생성/갱신
- 첫 빌드 자동 실행

## 2) 로컬 변경 감지 자동 배포
Linux/macOS:
```bash
bash ./scripts/local-autodeploy.sh
```

Windows PowerShell:
```powershell
./scripts/local-autodeploy.ps1
```

- 기본 동작: `MSA Manager` 미기동 시 `docker compose up -d` 자동 실행 후 감시 시작
- 변경 감지 시 변경 모듈만 빌드/배포
- 감시 제외: `target`, `logs`, `data`, `file`, `module/EgovMsaManager/runtime`

## 선택: GitHub webhook 자동 생성
실행 전에 토큰만 넣으면 webhook까지 자동 등록됩니다.

Linux/macOS:
```bash
export GITHUB_TOKEN=<your_token>
./scripts/jenkins-up.sh
```

Windows PowerShell:
```powershell
$env:GITHUB_TOKEN="<your_token>"
./scripts/jenkins-up.ps1
```

## 확인 주소
- Jenkins: http://localhost:18081
- Job: http://localhost:18081/job/carbosys-main-cicd/
- Manager: http://localhost:18030/admin/msa/manager
