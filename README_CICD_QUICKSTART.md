# CI/CD 빠른 시작 (옵션 없이 실행)

## 0) 실행 파일 (옵션 없음)
Linux/macOS:
```bash
./scripts/jenkins-up.sh
```

Windows PowerShell:
```powershell
./scripts/jenkins-up.ps1
```

로컬 폴더 변경 자동 빌드/배포 시작:

Linux/macOS:
```bash
./scripts/local-autodeploy.sh
```

Windows PowerShell:
```powershell
./scripts/local-autodeploy.ps1
```

중지:
```bash
./scripts/jenkins-down.sh
```

## 1) 바로 확인할 주소
- Jenkins: `http://localhost:18081`
- MSA Manager: `http://localhost:18030/admin/msa/manager`
- Gateway: `http://localhost:9000`

## 2) Jenkins 최초 비밀번호 (즉시 조회)
```bash
docker exec carbosys-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## 3) Jenkins Job 최소 설정
1. Pipeline Job 생성
2. `Pipeline script from SCM`
3. Repository: `https://github.com/sjkim0831/2026_carbosys.git`
4. Branch: `main`
5. Script Path: `Jenkinsfile`
6. Trigger: GitHub webhook 또는 Poll SCM

## 4) GitHub webhook 주소
- 기본 형식: `http://<운영서버주소>:18081/github-webhook/`
- 도메인 사용 시: `https://<도메인>/github-webhook/`

## 5) 동작 모드
- 운영 자동배포: `main` 변경 시 Jenkins 실행 (`CHANGE_SOURCE=git`)
- 로컬 자동배포: 프로젝트 폴더 변경 감지 (`CHANGE_SOURCE=working-tree`)

## 6) 배포 정책
- 일반 모듈: 무중단 배포 (`deploy-zerodowntime`)
- EgovMsaManager: 재시작 배포 (`deploy-restart`)
- 인프라 모듈(Eureka/Config/Gateway): 자동 대상 제외

## 7) 필요 정보 체크 명령 (지금 바로 실행 가능)
현재 컨테이너 상태:
```bash
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
```

MSA 모듈 상태:
```bash
curl -sS http://localhost:18030/admin/msa/api/modules
```

Jenkins 로그:
```bash
docker logs --tail 100 carbosys-jenkins
```
