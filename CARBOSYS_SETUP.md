# CUBRID Docker 실행 가이드

## 기준 파일 (Source of Truth)

- 실행/운영 기준은 **`docker-compose.yml`** 입니다.
- 이 문서와 값이 다르면 **항상 `docker-compose.yml`을 우선** 적용합니다.

## 핵심 변경 사항

- DB 파일 저장 위치는 프로젝트 상대경로 **`./data`** 기준입니다.
- CUBRID 데이터 디렉토리는 컨테이너 내부 `/var/lib/cubrid`에 `./data`를 바인드합니다.
- `docker compose down` / `up -d` 반복 시에도 `./data`가 유지되면 DB가 초기화되지 않습니다.
- 브로커는 `CUBRID_COMPONENTS=ALL` + `cubrid.conf(service=server,broker)` 기준으로 자동 기동됩니다.
- **외부 접속 지원**: 브로커(33000) 외에도 실제 데이터 처리를 담당하는 **CAS 포트(33001)**가 외부로 노출되어야 합니다.

---

## 데이터 경로 기준

리포지토리 루트(`/opt/carbosys`)에서 `docker compose`를 실행하는 것을 기준으로 `./data`를 사용합니다.

---

## 최초 설정 (한 번만)

### 1) 폴더 생성

```bash
mkdir -p infra/cubrid/conf infra/cubrid/init data
chmod 777 data
```

### 2) 설정 파일 확인

`infra/cubrid/conf/cubrid.conf`:

```conf
[%service]
service=server,broker

[%server]
master_port_id=1534

[%client]
```

`infra/cubrid/conf/cubrid_broker.conf`:

```conf
[%broker_name]
BROKER_PORT             =33000
MIN_NUM_APPL_SERVERS    =5
MAX_NUM_APPL_SERVERS    =40
ACCESS_MODE            =RW
APPL_SERVER_PORT       =33001
```

### 3) docker-compose 핵심 설정

```yaml
services:
  cubrid:
    environment:
      CUBRID_DB: com
      CUBRID_LOCALE: ko_KR.utf8
      CUBRID_COMPONENTS: ALL
      CUBRID_DATABASES: /var/lib/cubrid
    volumes:
      - "./infra/cubrid/init:/docker-entrypoint-initdb.d"
      - "./infra/cubrid/conf:/opt/carbosys/conf"
      - "./data:/var/lib/cubrid"
    ports:
      - "33000:33000"
      - "33001:33001"
```

---

## 실행

```bash
docker compose down
docker compose up -d
```

---

## DBeaver 접속 정보

- Host: `localhost`
- Port: `33000`
- Database: `com`
- User: `dba`
- Password: (비어있음)
- JDBC URL: `jdbc:cubrid:localhost:33000:com:::?charset=UTF-8`

---

## 문제 발생 시 점검 순서

1. 컨테이너/포트 상태 확인
```bash
docker compose ps
```

2. 브로커 기동 로그 확인
```bash
docker compose logs cubrid | tail -n 200
```

3. 컨테이너 내부에서 DB 파일 확인 (`/var/lib/cubrid/com*` 존재 여부)
```bash
docker exec cubrid ls -al /var/lib/cubrid
```

4. 호스트의 데이터 폴더 확인
```bash
ls -al ./data
```

5. 브로커 수동 기동(긴급 조치)
```bash
docker exec cubrid cubrid broker start
```

6. **Failed to connect... 에러 발생 시**
- `33001` 포트가 `docker-compose.yml`에 노출되어 있는지 확인하십시오.
- `cubrid_broker.conf`에 `APPL_SERVER_PORT = 33001`이 설정되어 있는지 확인하십시오.

---

## Project Hub 연동 메타

다중 프로젝트 관리 프로그램(`project-hub`)에서 이 프로젝트를 자동 인식하려면 아래 파일을 기준으로 연결합니다.

- `/opt/carbosys/ops-project.yml`
- `/opt/carbosys/.ops-control/project.json`

프로젝트를 복사해 새 프로젝트를 만들 때는 다음 값만 새 프로젝트명/경로에 맞게 변경하면 됩니다.

- `project.id`, `project.name`
- `paths.projectRoot`, `paths.moduleRoot`
- `paths.containerProjectPath`
- `.ops-control/project.json`의 `projectId`, `projectName`, `rootPath`, `modulePath`, `containerProjectPath`
