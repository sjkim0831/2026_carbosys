# CI/CD 한 번 실행 가이드

## Linux/macOS
```bash
./scripts/jenkins-up.sh
```

## Windows PowerShell
```powershell
./scripts/jenkins-up.ps1
```

위 1개 명령으로 아래가 자동 수행됩니다.
- Jenkins/앱 컨테이너 빌드 및 기동
- Jenkins 준비 대기
- `carbosys-main-cicd` Job 자동 생성/갱신
- 첫 빌드 자동 실행

## 선택: 웹훅까지 자동 생성
실행 전에 토큰만 넣으면 GitHub webhook까지 자동 등록됩니다.

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

## 참고
- 회사 네트워크에서 외부 webhook 수신하려면 18081 포트 인바운드 허용이 필요합니다.
- 현재 로컬/고시원망에서는 webhook delivery가 실패할 수 있습니다.
